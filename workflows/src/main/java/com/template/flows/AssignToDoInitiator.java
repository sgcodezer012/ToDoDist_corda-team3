package com.template.flows;

import co.paralleluniverse.fibers.Suspendable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.template.contracts.ToDoDistContract;
import com.template.states.ToDoDistState;
import net.corda.core.contracts.Command;
import net.corda.core.contracts.StateAndRef;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.flows.*;
import net.corda.core.identity.Party;
import net.corda.core.node.ServiceHub;
import net.corda.core.node.services.Vault;
import net.corda.core.node.services.vault.QueryCriteria;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.transactions.TransactionBuilder;
import net.corda.core.utilities.ProgressTracker;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@InitiatingFlow
@StartableByRPC
public class AssignToDoInitiator extends FlowLogic<SignedTransaction> {

    private final ProgressTracker progressTracker = new ProgressTracker();

    private String UID;
    private String assignedTo;
    public AssignToDoInitiator(String ID,
                               String assignedTo) {
        this.UID = ID;
        this.assignedTo = assignedTo;
    }

    @Override
    public ProgressTracker getProgressTracker() {
        return progressTracker;
    }

    @Suspendable
    @Override
    public SignedTransaction call() throws FlowException {

       /* final QueryCriteria q = new QueryCriteria.LinearStateQueryCriteria(null, ImmutableList.of(UUID.fromString(linearId)));
        final Vault.Page<ToDoState> taskStatePage = sb.getVaultService().queryBy(ToDoState.class, q);
        final List<StateAndRef<ToDoState>> states = taskStatePage.getStates();
        final StateAndRef<ToDoState> sar = states.get(0);
        final ToDoState toDoState = sar.getState().getData();
        Set<Party> parties = identityService.partiesFromName(this.counterParty, true);
        // Get Next Parties Iteration
        Party receiver = parties.iterator().next();

        FlowSession flowSession = initiateFlow(receiver);

        */
        ServiceHub serviceHub = getServiceHub();
        Party self = getOurIdentity(); // Self Party
        final Party notary = serviceHub.getNetworkMapCache().getNotaryIdentities().get(0); // Get Notary
        final QueryCriteria q = new QueryCriteria.LinearStateQueryCriteria(null, ImmutableList.of(UUID.fromString(this.UID)));
        final Vault.Page<ToDoDistState> taskStatePage = serviceHub.getVaultService().queryBy(ToDoDistState.class, q);
        final List<StateAndRef<ToDoDistState>> states = taskStatePage.getStates();
        final StateAndRef<ToDoDistState> sar = states.get(0);
        final ToDoDistState toDoState = sar.getState().getData();
        System.out.println("State" + toDoState);
        System.out.println("Sar" + sar);


        Set<Party> parties = serviceHub.getIdentityService().partiesFromName(this.assignedTo, true);
        Party assigned_to = parties.iterator().next();
        toDoState.setAssignTo(assigned_to);
        //Create Command with Signers
        final Command<ToDoDistContract.Commands.Create> txCommand = new Command<>(
                new ToDoDistContract.Commands.Create(),
                ImmutableList.of(toDoState.getAssignBy().getOwningKey(), assigned_to.getOwningKey())); // Create Command
        //Create Transaction Builder, Look up and add inputs / add outputs and Add command
        final TransactionBuilder txBuilder = new TransactionBuilder(notary)
                .addOutputState(toDoState, ToDoDistContract.ID)
                .addCommand(txCommand);

        //Validate the Task Description
        txBuilder.verify(serviceHub);

        //Send TransactionBuilder to ServiceHub for signing
        final SignedTransaction initialSignedTx = getServiceHub().signInitialTransaction(txBuilder);
        FlowSession assignToSession = initiateFlow(assigned_to);
        final SignedTransaction collectSignedTx = subFlow(new CollectSignaturesFlow(initialSignedTx, ImmutableSet.of(assignToSession), CollectSignaturesFlow.Companion.tracker()));
        final SignedTransaction fullySignedTx = subFlow(new FinalityFlow(collectSignedTx, ImmutableSet.of(assignToSession)));
        return fullySignedTx;
    }
}

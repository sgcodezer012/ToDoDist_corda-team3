package com.template.flows;

import co.paralleluniverse.fibers.Suspendable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.template.contracts.ToDoDistContract;
import com.template.states.ToDoDistState;
import net.corda.core.contracts.Command;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.flows.*;
import net.corda.core.identity.Party;
import net.corda.core.node.ServiceHub;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.transactions.TransactionBuilder;
import net.corda.core.utilities.ProgressTracker;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

// ******************
// * Initiator flow *
// ******************
@InitiatingFlow
@StartableByRPC
public class ToDoDistInitiator extends FlowLogic<SignedTransaction> {
    private final ProgressTracker progressTracker = new ProgressTracker();

    private LocalDate currentDate;
    private String taskDescription;
    private String taskStatus;
    public ToDoDistInitiator(String taskDescription,
                             String taskStatus) {
        this.taskDescription = taskDescription;
        this.taskStatus = taskStatus;
        this.currentDate = LocalDate.now();
    }
    @Override
    public ProgressTracker getProgressTracker() {
        return progressTracker;
    }

    @Suspendable
    @Override
    public SignedTransaction call() throws FlowException {
        ServiceHub serviceHub = getServiceHub();
        Party self = getOurIdentity(); // Self Party
        final ToDoDistState toDoDistState = new ToDoDistState(self, self, taskDescription, taskStatus, currentDate); // Initialize State
        final Party notary = serviceHub.getNetworkMapCache().getNotaryIdentities().get(0); // Get Notary
        //Create Command with Signers
        final Command<ToDoDistContract.Commands.Create> txCommand = new Command<>(
                new ToDoDistContract.Commands.Create(),
                ImmutableList.of(toDoDistState.getAssignBy().getOwningKey(), toDoDistState.getAssignTo().getOwningKey())); // Create Command
        //Create Transaction Builder, Look up and add inputs / add outputs and Add command
        final TransactionBuilder txBuilder = new TransactionBuilder(notary)
                .addOutputState(toDoDistState, ToDoDistContract.ID)
                .addCommand(txCommand);

        //Validate the Task Description
        txBuilder.verify(serviceHub);

        //Send TransactionBuilder to ServiceHub for signing
        final SignedTransaction initialSignedTx = getServiceHub().signInitialTransaction(txBuilder);

        // Signed Owned and Store to DB
        final SignedTransaction fullySignedTx = subFlow(new FinalityFlow(initialSignedTx, Arrays.asList()));
        return fullySignedTx;
    }
}

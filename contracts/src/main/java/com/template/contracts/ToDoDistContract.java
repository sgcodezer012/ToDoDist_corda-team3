package com.template.contracts;

import com.template.states.ToDoDistState;
import net.corda.core.contracts.CommandData;
import net.corda.core.contracts.CommandWithParties;
import net.corda.core.contracts.Contract;
import net.corda.core.transactions.LedgerTransaction;

import static net.corda.core.contracts.ContractsDSL.requireSingleCommand;
import static net.corda.core.contracts.ContractsDSL.requireThat;

// ************
// * Contract *
// ************
public class ToDoDistContract implements Contract {
    // This is used to identify our contract when building a transaction.
    public static final String ID = "com.template.contracts.ToDoDistContract";

    // A transaction is valid if the verify() function of the contract of all the transaction's input and output states
    // does not throw an exception.
    @Override
    public void verify(LedgerTransaction tx) {
       final CommandWithParties<Commands.Create> command = requireSingleCommand(tx.getCommands(), Commands.Create.class);

        requireThat(require -> {
            final ToDoDistState out = tx.outputsOfType(ToDoDistState.class).get(0);
            boolean isTask = out.getTaskDescription().length() <= 40;
            boolean isEmpty = out.getTaskDescription().isEmpty();

            require.using("Task Description should not be more than 40 Characters", isTask);
            require.using("Task Description should not be not be blank", !isEmpty);

            System.out.println("TrxID: " + out.getLinearId());
            System.out.println("Created: " + out.getDateOfCreation());
            System.out.println("AssignedBy: " + out.getAssignBy());
            System.out.println("AssignedTo: " + out.getAssignTo());
            System.out.println("Task: " + isTask);
            System.out.println("Verify Completed");
            System.out.println("===============================");

            return null;
        });
    }

    // Used to indicate the transaction's intent.
    public interface Commands extends CommandData {
        class Create implements Commands {}
    }
}
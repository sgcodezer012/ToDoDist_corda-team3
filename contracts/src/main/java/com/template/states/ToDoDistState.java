package com.template.states;

import com.template.contracts.ToDoDistContract;
import net.corda.core.contracts.BelongsToContract;
import net.corda.core.contracts.ContractState;
import net.corda.core.contracts.LinearState;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.identity.AbstractParty;
import net.corda.core.identity.Party;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

// *********
// * State *
// *********
@BelongsToContract(ToDoDistContract.class)
public class ToDoDistState implements LinearState {
    private Party assignedBy;
    private Party assignedTo;
    private String taskDescription;
    private String taskStatus;
    private UniqueIdentifier linearId;
    private LocalDate dateOfCreation;

    public ToDoDistState(Party assignedBy,
                         Party assignedTo,
                         String taskDescription,
                         String taskStatus,
                         LocalDate dateOfCreation) {
        this.linearId = new UniqueIdentifier();
        this.assignedBy = assignedBy;
        this.assignedTo = assignedTo;
        this.taskDescription = taskDescription;
        this.taskStatus = taskStatus;
        this.dateOfCreation = dateOfCreation;
    }
    public String getTaskDescription() { return taskDescription; }
    public LocalDate getDateOfCreation() { return dateOfCreation;}
    public String getTaskStatus() { return taskStatus;}
    public Party getAssignBy() { return assignedBy;}
    public Party getAssignTo() { return assignedTo;}

    @Override
    public List<AbstractParty> getParticipants() {
        return Arrays.asList(this.assignedBy, this.assignedTo);
    }

    @NotNull
    @Override
    public UniqueIdentifier getLinearId() {
        return new UniqueIdentifier();
    }
}
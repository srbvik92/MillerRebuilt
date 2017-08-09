package projectObjects;

import java.util.Date;


import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

/**
 * @author jmackin
 * @author Andrew Serensits (JMJ)
 *
 */
@Entity
public class Task extends ProjectObject implements Comparable<Task> {
	private String title;
	private String description;
	private Project project;
	private int severity;
	private Date dueDate;
	private Date assignedDate;
	private User assigner;
	private User assignee;
	private boolean completed;
	private String notes;
	private TaskStatus status;
	
	
	public Task (String title, String description, Project p, int s, Date due, 
			Date assigned, User assigner, User assignee, boolean c, String notes, TaskStatus status) {
		this.title = title;
		this.description = description;
		this.project = p;
		this.severity = s;
		this.dueDate = due;
		this.assignedDate = assigned;
		this.assigner = assigner;
		this.assignee = assignee;
		this.completed = c;
		this.setNotes(notes);
		this.status = status;
	}
	
	public Task() {
		title = null;
		description = null;
		project = null;
		severity = -1;
		dueDate = null;
		assignedDate = null;
		assigner = null;
		assignee = null;
		completed = false;
		setNotes(null);
		status = null;
	}
	
	public synchronized String getTitle() {
		return title;
	}
	public synchronized void setTitle(String title) {
		this.title = title;
	}
	public synchronized String getDescription() {
		return description;
	}
	public synchronized void setDescription(String description) {
		this.description = description;
	}
	
	@ManyToOne(fetch = FetchType.EAGER)
	@Fetch(FetchMode.SELECT)
	public synchronized Project getProject() {
		return project;
	}
	public synchronized void setProject(Project project) {
		this.project = project;
	}
	public synchronized int getSeverity() {
		return severity;
	}
	public synchronized void setSeverity(int severity) {
		this.severity = severity;
	}
	public synchronized Date getDueDate() {
		return dueDate;
	}
	public synchronized void setDueDate(Date dueDate) {
		this.dueDate = dueDate;
	}
	public synchronized Date getAssignedDate() {
		return assignedDate;
	}
	public synchronized void setAssignedDate(Date assignedDate) {
		this.assignedDate = assignedDate;
	}
	
	@ManyToOne(fetch = FetchType.EAGER)
	@Fetch(FetchMode.SELECT)
	public synchronized User getAssigner() {
		return assigner;
	}
	public synchronized void setAssigner(User assigner) {
		this.assigner = assigner;
	}
	
	@ManyToOne(fetch = FetchType.EAGER)
	@Fetch(FetchMode.SELECT)
	public synchronized User getAssignee() {
		return assignee;
	}
	public synchronized void setAssignee(User assignee) {
		if(assignee == null) System.out.println("IT WAS NULLLLL");
		this.assignee = assignee;
	}
	public synchronized boolean isCompleted() {
		return completed;
	}
	public synchronized void setCompleted(boolean completed) {
		this.completed = completed;
	}
	
	public synchronized String getNotes() {
		return notes;
	}

	public synchronized void setNotes(String notes) {
		this.notes = notes;
	}
	
	@ManyToOne(fetch = FetchType.EAGER)
	@Fetch(FetchMode.SELECT)
	public synchronized TaskStatus getTaskStatus() {
		return status;
	}
	
	public synchronized void setTaskStatus(TaskStatus status) {
		this.status = status;
	}
	
	public synchronized int compareTo(Task compareTask) {
		return compareStrings(this.getAssignee().getFirstName(), compareTask.getAssignee().getFirstName());
	}
	
	public synchronized int compareStrings(String str1, String str2) {
		str1 = str1.toLowerCase();
		str2 = str2.toLowerCase();
		char[] arr1 = str1.toCharArray();
		char[] arr2 = str2.toCharArray();
		int i = 0;
		while(i < arr1.length && i < arr2.length){
			if(arr1[i] < arr2[i]) return -1;
			else if(arr1[i] > arr2[i]) return 1;
			i++;
		}
		return 0;
	}

	public synchronized String toString() {
		return this.title + ": " + this.description;
	}
}

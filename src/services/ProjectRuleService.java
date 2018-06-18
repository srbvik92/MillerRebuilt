package services;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gson.JsonObject;

import projectObjects.ProjectRule;
import projectObjects.RuleDomain;
import projectObjects.RuleResult;
import projectObjects.Task;
import projectObjects.TaskStatus;
import projectObjects.ChangeOrder;
import projectObjects.CloseoutDetails;
import projectObjects.CloseoutStatus;
import projectObjects.NewEquipment;
import projectObjects.Permits;
import projectObjects.Project;
import projectObjects.RuleDomain;
import projectObjects.RuleSeverity;

public class ProjectRuleService 
{
		
	/**
	 * 
	 * @param d1
	 * @param d2
	 * @return RuleResult
	 */
	public static RuleResult EvaluateDates(Date d1 , Date d2)
	{
		if(d1 == null && d2 == null)
			return RuleResult.DD_NN;
		else if(d1 == null && d2 != null)
			return RuleResult.DD_NV;
		else if(d1 != null && d2 == null)
			return RuleResult.DD_VN;
		else
		{
			int result = d1.compareTo(d2);
			
			if(result == 0 || result == -1)
				return RuleResult.DD_LATE;
			else
				return RuleResult.DD_EARLY;
		}
	}
	
	public static RuleResult EvaluateNumbers(Double d1 , Double d2)
	{
		if(d1 == null && d2 == null)
			return RuleResult.NN_NN;
		else if(d1 == null && d2 != null)
			return RuleResult.NN_NV;
		else if(d1 != null && d2 == null)
			return RuleResult.NN_VN;
		else
		{
			int result = d1.compareTo(d2);
			
			if(result == 0)
				return RuleResult.NN_EQUAL;
			else if(result == -1)
				return RuleResult.NN_LESS;
			else
				return RuleResult.NN_GREATER;
		}
	}
	
	public static RuleResult EvaluateDateNumber(Date d1 , Double d2)
	{
		if(d1 == null && d2 == null)
			return RuleResult.DN_NN;
		else if(d1 == null && d2 != null)
			return RuleResult.DN_NV;
		else
			return RuleResult.DN_VN;
	}
	
	public static RuleResult EvaluateNumberDate(Double d1 , Date d2)
	{
		if(d1 == null && d2 == null)
			return RuleResult.ND_NN;
		else if(d1 == null && d2 != null)
			return RuleResult.ND_NV;
		else
			return RuleResult.ND_VN;
	}
	
	public static RuleResult EvaluateStrings(String d1 , String d2)
	{
		if(d1 == null && d2 == null)
			return RuleResult.SS_NN;
		else if(d1 == null && d2 != null)
			return RuleResult.SS_NV;
		else if(d1 != null && d2 == null)
			return RuleResult.SS_VN;
		else if(d1.isEmpty() && d2.isEmpty())
			return RuleResult.SS_NN;
		else if(d1.isEmpty() && !(d2.isEmpty()))
			return RuleResult.SS_NV;
		else if(!(d1.isEmpty()) && !(d2.isEmpty()))
			return RuleResult.SS_VN;
		else
			return RuleResult.SS_VV;
	}
	
	public static RuleResult EvaluateTask(Task task)
	{
		TaskStatus status = task.getTaskStatus();
		Date dueDate = task.getDueDate();
		Date today = new Date();

		if(dueDate == null)
			return RuleResult.TASK_NULL_DUE;
				
		if(status == null)
			return RuleResult.TASK_NULL_STATUS;
				
		int result = dueDate.compareTo(today);
		
		if(result <= 0)
			return RuleResult.TASK_ONTIME;
		else if( task.getTaskStatus().getStatus().equalsIgnoreCase("Open"))
			return RuleResult.TASK_LATE;
		else
			return RuleResult.TASK_ONTIME;
			
	}
	
	public static RuleResult EvaluateCloseoutStatus(String status)
	{
		if(status == null)
			return RuleResult.CLOSEOUT_NULL_STATUS;
		if(status.isEmpty())
			return RuleResult.CLOSEOUT_NULL_STATUS;
		
		if(status.equalsIgnoreCase("complete"))
			return RuleResult.CLOSEOUT_COMPLETE;
		else if(status.equalsIgnoreCase("incomplete"))
			return RuleResult.CLOSEOUT_INCOMPLETE;
		else if(status.equalsIgnoreCase("N/A"))
			return RuleResult.CLOSEOUT_NA;
		
		return null;
	}
	
	public static boolean CloseoutEvaluate(ProjectRule _rule , CloseoutDetails _co )
	{
		CloseoutDetails co = _co;
		
		String f1 , f2;
		f1 = _rule.getField1();
		f2 = _rule.getField2();
		
		Object field1 = CloseoutDetails.getCloseoutFields(f1 , co);
		Object field2 = CloseoutDetails.getCloseoutFields(f2 , co);
		
		if(field1 instanceof String || field1 == null)
			return _rule.evaluate(EvaluateCloseoutStatus((String) field1));
		
		
		if(field1 instanceof Double && field2 instanceof Double)
			return _rule.evaluate(EvaluateNumbers((Double) field1 , (Double) field2));
		
		
		return false;
		
	}
	
	public static boolean ChangeOrderEvaluate(ProjectRule _rule , ChangeOrder _co)
	{
		ChangeOrder co = _co;
		if(co == null)
			return false;
		
		String f1 , f2;
		f1 = _rule.getField1();
		f2 = _rule.getField2();
		
		Object field1 = ChangeOrder.getChangeOrderFields(f1 , co);
		Object field2 = ChangeOrder.getChangeOrderFields(f2 , co);
		
		if(field1 instanceof Date && field2 instanceof Date)
			return _rule.evaluate(EvaluateDates((Date) field1 , (Date) field2));
		else if(field1 instanceof Date && field2 == null)
			return _rule.evaluate(EvaluateDates((Date) field1 , null));
		else if(field1 == null && field2 instanceof Date)
			return _rule.evaluate(EvaluateDates(null , (Date) field2));
		
		if(field1 instanceof Double && field2 instanceof Double)
			return _rule.evaluate(EvaluateNumbers((Double) field1 , (Double) field2));
		else if(field1 instanceof Double && field2 == null)
			return _rule.evaluate(EvaluateNumbers((Double) field1 , null));
		else if(field1 == null && field2 instanceof Double)
			return _rule.evaluate(EvaluateNumbers(null , (Double) field2));
		
		return false;
	}
	
	public static boolean FinancialEvaluate(ProjectRule _rule , Project _proj)
	{
		Project proj = _proj;
		
		if(proj == null)	proj = _rule.getProject();
		//Maybe handle it elsewhere if the rule is newly created?
		
		if(proj == null) return false;
		
		String f1 , f2;
		f1 = _rule.getField1();
		f2 = _rule.getField2();
		
		Double field1 = Project.getFinancialFields(f1 , proj);
		Double field2 = Project.getFinancialFields(f2 , proj);
		
		return _rule.evaluate(EvaluateNumbers(field1 , field2));
	}
	
	public static boolean TaskEvaluate(ProjectRule _rule , Task _task)
	{
		Task task = _task;
		if(task == null)
			return false;
		
		String f1 , f2;
		f1 = _rule.getField1();
		f2 = _rule.getField2();
		
		Object field1 = Task.getTaskFields(f1 , task);
		Object field2 = Task.getTaskFields(f2 , task);
		
		return _rule.evaluate(EvaluateTask(task));

	}
	
	public static boolean SchedulingEvaluate(ProjectRule _rule , Project _proj)
	{
		Project proj = _proj;
		
		if(proj == null)	proj = _rule.getProject();
		//Maybe handle it elsewhere if the rule is newly created?
		
		if(proj == null) return false;
		
		String f1 , f2;
		f1 = _rule.getField1();
		f2 = _rule.getField2();
		
		Date field1 = Project.getSchedulingFields(f1 , proj);
		Date field2 = Project.getSchedulingFields(f2 , proj);
		

		return _rule.evaluate(EvaluateDates(field1 , field2));
		
				
				
	}
	
	public static boolean EquipmentEvaluate(ProjectRule _rule , NewEquipment _eq)
	{
		NewEquipment eq = null;
		
		if(_eq == null)	return true;
		
		//Maybe handle it elsewhere if the rule is newly created?
		
		eq = _eq;
		
		String f1 , f2;
		f1 = _rule.getField1();
		f2 = _rule.getField2();
		
		Object field1 = NewEquipment.getNewEquipmentFields(f1 , eq);
		Object field2 = NewEquipment.getNewEquipmentFields(f2 , eq);
		

		if(field1 instanceof Date && field2 instanceof Date)
			return _rule.evaluate(EvaluateDates((Date) field1 , (Date) field2));
		else if(field1 instanceof Date && field2 == null)
			return _rule.evaluate(EvaluateDates((Date) field1 , null));
		else if(field1 == null && field2 instanceof Date)
			return _rule.evaluate(EvaluateDates(null , (Date) field2));
		
		return false;

	}
	
	public static boolean PermitAndInspectionEvaluate(ProjectRule _rule , Project _proj)
	{
		Project proj = _proj;
		
		if(proj == null)	proj = _rule.getProject();
		//Maybe handle it elsewhere if the rule is newly created?
		
		Permits perms = proj.getPermits();
		if(perms == null)
			return true;

		String f1 , f2;
		f1 = _rule.getField1();
		f2 = _rule.getField2();
		
		Object field1 = Permits.getPermitAndInspectionFields(f1 , perms);
		Object field2 = Permits.getPermitAndInspectionFields(f2 , perms);
				
		


		if(field1 instanceof Date && field2 instanceof Date)
			return _rule.evaluate(EvaluateDates((Date) field1 , (Date) field2));
		else if(field1 instanceof String && field2 instanceof String)
			return _rule.evaluate(EvaluateStrings((String) field1 , (String) field2));
		else if(field1 instanceof String && field2 == null)
			return _rule.evaluate(EvaluateStrings((String) field1 , null));
		else if(field1 == null && field2 instanceof String)
			return _rule.evaluate(EvaluateStrings( null , (String) field2));
		else if(field1 instanceof Date && field2 == null)
			return _rule.evaluate(EvaluateDates((Date) field1 , null));
		else if(field1 == null && field2 instanceof Date)
			return _rule.evaluate(EvaluateDates(null , (Date) field2));
		
		return false;
		
	}
	
	public static Map<String , Object> EvaluateProject(List<ProjectRule> rules , Project project)
	{
		if(project == null || rules == null) return null;
		
		Map<String , Object> evaluation = new HashMap<String , Object>();
		evaluation.put("PROJECT", project);
		project.setLowScore(0);
		project.setMediumScore(0);
		project.setHighScore(0);
		
		for(ProjectRule rule : rules)
		{
			Map<String , Object> map = new HashMap<String , Object>();
			//evaluation.put(rule.getId().toString() , map);
			evaluation.put(rule.getTitle() , map);
			map.put("RULE_ID", rule.getId());
			map.put("RULE_TITLE", rule.getTitle());
			map.put("RULE_DOMAIN", rule.getDomain());

			if(rule.getProjectClass() == null || 
				 (rule.getProjectClass() != null && rule.getProjectClass().getName().equalsIgnoreCase(project.getProjectClass().getName())))
			{
				Boolean result = null;
				switch(rule.getDomain())
				{
					case PermitsAndInspections:
						result = PermitAndInspectionEvaluate(rule , project);
						map.put("type", "PermitsAndInspections");
						break;
					case Scheduling:
						result = SchedulingEvaluate(rule , project);
						map.put("siteSurvey", project.getSiteSurvey());
						map.put("budgetaryDue" , project.getBudgetaryDue());
						map.put("budgetarySubmitted", project.getBudgetarySubmitted());
						map.put("proposalDue", project.getProposalDue());
						map.put("proposalSubmitted", project.getProposalSubmitted());
						map.put("scheduledStartDate", project.getScheduledStartDate());
						map.put("projectInitiatedDate", project.getProjectInitiatedDate());
						map.put("scheduledTurnover", project.getScheduledTurnover());
						map.put("actualTurnover", project.getActualTurnover());
						map.put("permitApp" , project.getPermitApplication());
						map.put("type", "Scheduling");
						break;
					case Tasks:
						Map<String , Object> taskMap = EvaluateProjectTasks(rule , project);
						map.put("taskResults", taskMap);
						map.put("type", "Task");
						break;
					case Financial:
						result = FinancialEvaluate(rule , project);
						map.put("shouldInvoice", project.getShouldInvoice());
						map.put("actualInvoice", project.getInvoiced());
						map.put("cost", project.getCost());
						map.put("type", "Financial");
						break;
					case ChangeOrders:
						Map<String , Object> changeOrderMap = EvaluateProjectChangeOrders(rule , project);
						map.put("changeOrderResults", changeOrderMap);
						map.put("type", "ChangeOrders");
						break;
					case Closeout:
						result = CloseoutEvaluate(rule , project.getCloseoutDetails());
						map.put("closeoutDetails", project.getCloseoutDetails());
						map.put("type", "Closeout");
						break;
					case Equipment:
						Map<String , Object> equipmentMap = EvaluateProjectChangeOrders(rule , project);
						map.put("equipmentResults", equipmentMap);
						map.put("type", "Equipment");
						break;
					default:
						break;
				}
				if(result != null)
				{
					if(result == true) {
						map.put("message" , rule.getPassMessage());
						map.put("passed", "true");
					}
					else 
					{
						switch(rule.getSeverity())
						{
							case LOW:
								project.setLowScore(project.getLowScore() + 1);
								break;
							case MEDIUM:
								project.setMediumScore(project.getMediumScore() + 1);
								break;
							case HIGH:
								project.setHighScore(project.getHighScore() + 1);
								break;
						}
						map.put("message", rule.getFailMessage());
						map.put("passed", "false");
					}
				}
					
			}
		}
		
		return evaluation;
		
	}
	
	public static Map<String , Object> EvaluateProjectTasks(ProjectRule rule , Project project)
	{
		if(rule == null || project == null)
			return null;
		
		Map<String , Object> map = new HashMap<String , Object>();
		List<Task> tasks = ProjectObjectService.getAllTasks(project.getId());
		map.put("TASKS", tasks);
		for(Task task : tasks)
		{
			boolean result;
			Map<String , Object> results = new HashMap<String , Object>();
			map.put(task.getId().toString(), results);
			
			result = TaskEvaluate(rule , task);
			
			results.put("title" , task.getTitle());
			if(task.getAssignee() == null)
				results.put("assignee", task.getSubAssignee());
			else
				results.put("assignee", task.getAssignee());
			results.put("dueDate", task.getDueDate());
			results.put("description", task.getDescription());
			results.put("notes", task.getNotes());
			results.put("status", task.getTaskStatus());
										
			if(result == true) {
				results.put("message" , rule.getPassMessage());
				results.put("passed", "true");
			}
			else {
				results.put("message" , rule.getFailMessage());
				results.put("passed", "false");
			}
		}
		
		return map;
		
	}
	
	public static Map<String , Object> EvaluateProjectChangeOrders(ProjectRule rule , Project project)
	{
		if(rule == null || project == null)
			return null;
		
		Map<String , Object> map = new HashMap<String , Object>();
		Set<ChangeOrder> changeOrders = project.getChangeOrders();
		

		for(ChangeOrder co : changeOrders)
		{
			boolean result;
			Map<String , Object> results = new HashMap<String , Object>();
			map.put(co.getId().toString(), results);
			
			result = ChangeOrderEvaluate(rule , co);
			
			results.put("title" , co.getTitle());
			results.put("proposalDate", co.getProposalDate());
			results.put("submittedDate", co.getSubmittedDate());
			results.put("approvedDate", co.getApprovedDate());
			results.put("cost" , co.getCost());
			results.put("sell", co.getSell());
			results.put("submittedTo", co.getSubmittedTo());
			results.put("description", co.getBriefDescription());
			results.put("notes", co.getNotes());
			results.put("status", co.getStatus());
										
			if(result == true) {
				results.put("message" , rule.getPassMessage());
				results.put("passed", "true");
			}
			else {
				results.put("message" , rule.getFailMessage());
				results.put("passed", "false");
			}
		}
		
		return map;
		
	}
	
	public static Map<String , Object> EvaluateProjectEquipment(ProjectRule rule , Project project)
	{
		if(rule == null || project == null)
			return null;
		
		Map<String , Object> map = new HashMap<String , Object>();
		Set<NewEquipment> equipment = project.getProjEquipment();
		

		for(NewEquipment eq : equipment)
		{
			boolean result;
			Map<String , Object> results = new HashMap<String , Object>();
			map.put(eq.getId().toString(), results);
			
			result = EquipmentEvaluate(rule , eq);
			
			results.put("title" , eq.getEquipmentName());
			results.put("orderedDate", eq.getOrderedDate());
			results.put("deliveryDate", eq.getDeliveryDate());
			results.put("estDeliveryDate", eq.getEstDeliveryDate());
			results.put("vendor" , eq.getVendor());
			results.put("description", eq.getDescription());
			results.put("notes", eq.getNotes());
			results.put("deliveryStatus", eq.getDeliveryStatus());
			results.put("eqStatus", eq.getEqStatus());
										
			if(result == true) {
				results.put("message" , rule.getPassMessage());
				results.put("passed", "true");
			}
			else {
				results.put("message" , rule.getFailMessage());
				results.put("passed", "false");
			}
			
		}
		
		return map;
		
	}


}
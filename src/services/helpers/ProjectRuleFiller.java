package services.helpers;

import java.text.ParseException;
import java.util.Map;

import projectObjects.ProjectClass;
import projectObjects.ProjectRule;
import projectObjects.RuleDomain;
import projectObjects.RuleResult;
import projectObjects.RuleSeverity;
import services.ProjectObjectService;

public class ProjectRuleFiller 
{
	public synchronized static void fillRule(ProjectRule rule,  Map<String, String>params) throws ParseException , ClassNotFoundException
	{
		
		RuleDomain domain = RuleDomain.parseDomain(params.get("domain"));
		RuleSeverity priority = RuleSeverity.parseSeverity(Integer.parseInt(params.get("severity")));
		RuleResult result = RuleResult.parseRuleGoal(params.get("goal"));
		String field1 = params.get("field1");
		String field2 = params.get("field2");
		String title = params.get("title");
		String passMessage = params.get("passMessage");
		String failMessage = params.get("failMessage");
		
		rule.setDomain(domain);
		rule.setSeverity(priority);
		rule.setGoal(result);
		rule.setField1(field1);
		rule.setField2(field2);
		rule.setTitle(title);
		rule.setPassMessage(passMessage);
		rule.setFailMessage(failMessage);
		if(params.get("projectClass").equalsIgnoreCase("none"))
			rule.setProjectClass(null);
		else
			rule.setProjectClass((ProjectClass) ProjectObjectService.get(Long.parseLong(params.get("projectClass")), "ProjectClass"));

		
		
	}

}
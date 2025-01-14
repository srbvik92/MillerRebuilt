package Servlets;

import java.io.IOException;

import java.io.PrintWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.Session;
import org.hibernate.Transaction;

import com.google.gson.Gson;

import objects.HibernateUtil;
import objects.RequestHandler;
import projectObjects.ChangeOrder;
import projectObjects.City;
import projectObjects.NewEquipment;
import projectObjects.PendingInvoice;
import projectObjects.Invoice;
import projectObjects.ProjectRule;
import projectObjects.Region;
import projectObjects.RuleDetails;
import projectObjects.RuleDomain;
import projectObjects.RuleResult;
import projectObjects.RuleSeverity;
import projectObjects.State;
import projectObjects.Subcontractor;
import projectObjects.Task;
import projectObjects.User;
import projectObjects.Warehouse;
import services.LoginService;
import services.ProjectNewRuleColorService;
import services.ProjectNewRuleService;
import services.ProjectObjectService;
import services.ProjectRuleService;
import services.ProjectService;
import services.QueryService;
import services.helpers.PendInvFiller;
import services.helpers.InvoiceFiller;
import services.helpers.TaskFiller;
import objects.HashGen;
import java.util.*;
import javax.mail.*;
import javax.mail.internet.*;




/**
 * Always code as if the guy who ends up maintaining your code 
 * will be a violent psychopath who knows where you live.
 * 
 *  -- Never Forgetti the Quoteroli 
 * 
 */
@WebServlet("/Project")
public class Project extends HttpServlet 
{
	private static final long serialVersionUID = 1L;
	private static PrintWriter out;
       
    public Project() 
    {
    	
        super();
    }

    /**
     * TODO: This is pretty sloppy, if we could separate into different servlets or 
     * 				normalize the way we do stuff that would be great. It's just all over the place right now. 
     * 
     * This is where most of the java work is called from. The whatever.js
     * page will send a request to this URL given a set of parameters with 
     * an action. This method will result in data being written back out to 
     * the caller. 
     * 
	*/
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException 
	{
		if(!LoginService.verify(req)) { resp.setContentType("plain/text"); out = resp.getWriter(); out.println("VERIFICATION_FAILURE"); return;}
		resp.setContentType("application/json");
		out = resp.getWriter();
		String response = "";
		Map<String, String> parameters = RequestHandler.getParameters((req.getParameterMap()));
		
		//Get the domain and desired action
		//String domain = parameters.get("domain");
		String action = parameters.get("action");
		String timeStamp = new SimpleDateFormat("[MM/dd/yyyy] @ HH.mm.ss").format(new java.util.Date());
		System.out.println("SERVLET: Project.java\nIN: doPost()\nTime of transaction: " + timeStamp);
		if(action != null) System.out.println("ACTION: " + action);
		
		
		if(action == null) {
			resp.setContentType("plain/text"); 
			out = resp.getWriter(); 
			out.println("ACTION_FAILURE"); 
			return;
		}
		if (action.equals("getAllObjects"))
		{
			//System.out.println("get All Objects");
			response = ProjectService.getAllEnumsAsJson();
			
		} else if(action.equals("sendTaskAlert")) {
			//System.out.println("SENDING TASK ALERT");
	         String assignee = parameters.get("assignee");
	         System.out.println("ASSIGNEE: " + assignee);
	         User employeeRecipient = User.mapNameToUser(assignee);
	         Subcontractor subRecipient = Subcontractor.mapNameToSubcontractor(assignee);
			// Recipient's email ID needs to be mentioned.
		      String to;
		      if(employeeRecipient == null)
		    	  to = subRecipient.getEmail();
		      else
		    	  to = employeeRecipient.getEmail();
		      
		      // Sender's email ID needs to be mentioned
		      String from = "mcs.project.db@gmail.com";
		      //This password was missing, Akash added it
		      String pass = "MCS@1234";
		      // Assuming you are sending email from localhost
		      // before host = "localhost". Clearly that did not work. Updated the host by contacting Justin in IT
		      String host = "smtp.gmail.com";
//		      String host = "west.exch032.serverdata.net";	

		      // Get system properties
		      Properties properties = System.getProperties();

		      // Setup mail server
		      properties.put("mail.smtp.starttls.enable", "true");

		      properties.put("mail.smtp.ssl.trust", host);
		      properties.put("mail.smtp.user", from);
		      properties.put("mail.smtp.password", pass);
		      properties.put("mail.smtp.port", "587");
		      properties.put("mail.smtp.auth", "true");


		      // Get the default Session object.
		      javax.mail.Session session = javax.mail.Session.getDefaultInstance(properties);

		      try {
		         // Create a default MimeMessage object.
		         MimeMessage message = new MimeMessage(session);

		         // Set From: header field of the header.
		         message.setFrom(new InternetAddress(from));

		         // Set To: header field of the header.
		         message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));

		         // Set Subject: header field
		         String subject, body;
		        
		         subject = "MCS Task Alert";		        
		         String projectItem = parameters.get("projectItem");
		         String warehouseCity = parameters.get("warehouseCity");
		         String warehouseID = parameters.get("warehouseID");
		         String warehouseState = parameters.get("warehouseState");
		         String severity = parameters.get("severity");
		         String dueDate = parameters.get("dueDate");
		         String description = parameters.get("description");
		         String subAssignee = parameters.get("subAssignee");
		         String notes = parameters.get("notes");
		         String title = parameters.get("title");
		         
		         if(dueDate != null ) {
		         System.out.println("Proj Item: "+projectItem+"\nDescription: "+description+"\nCity: "+warehouseCity+"\nID: "+warehouseID+
		        		 "\nState: "+warehouseState+"\nSeverity: "+severity); 
		         }
		         else {
		        	 System.out.println("Proj Item: "+projectItem+"\nDescription: "+description+"\nCity: "+warehouseCity+"\nID: "+warehouseID+
			        		 "\nState: "+warehouseState+"\nSeverity: "+severity+"\nDue Date: "+dueDate); 
		         }

		         
		         if(dueDate != null && !dueDate.equals("")) {
		         body = "Project:  " + warehouseCity + ", " + warehouseState +
		                " --- " + projectItem+ "\n\nTitle: " + title + "\n\nWarehouse #: " + warehouseID+"\n\nDescription: "+description  + "\n\nAssignee: "+subAssignee+ "\n\nPriority: " + severity + "\n\nDue Date: " +
		                dueDate+ "\n\nNotes: " + notes ;
		         } else {
		        	 body = "Project:  " + warehouseCity + ", " + warehouseState +
					            " --- " + projectItem+"\n\nTitle: " + title + "\n\nWarehouse #: " + warehouseID+"\n\nDescription: "+description  + "\n\nAssignee: "+subAssignee+ "\n\nPriority: " + severity+ "\n\nNotes: " + notes ;
		         }
		         

		         message.setSubject(subject);

		         // Now set the actual message
		         message.setText(body);

		         // Send message
		         Transport transport = session.getTransport("smtp");
		         transport.connect(host, from, pass);
		         transport.sendMessage(message, message.getAllRecipients());
		         transport.close();
//		         Transport.send(message);
		         System.out.println("Sent message successfully....");
		      }catch (MessagingException mex) {
		         mex.printStackTrace();
		      } catch(Exception e) {
		    	  e.printStackTrace();
		      }
		      
		}
		
		else if(action.equals("sendInvoiceAlert")) {

			//System.out.println("SENDING INVOICE ALERT");
	         
	         
	       
	         
			// Recipient's email ID needs to be mentioned.
		      String to;
		    
		      to = "akashgurram18@gmail.com";
		      
		      // Sender's email ID needs to be mentioned
		      String from = "mcs.project.db@gmail.com";
		      //This password was missing, Akash added it
		      String pass = "MCS@1234";
		      // Assuming you are sending email from localhost
		      // before host = "localhost". Clearly that did not work. Updated the host by contacting Justin in IT
		      String host = "smtp.gmail.com";

		      // Get system properties
		      Properties properties = System.getProperties();

		      // Setup mail server
		      properties.put("mail.smtp.starttls.enable", "true");

		      properties.put("mail.smtp.ssl.trust", host);
		      properties.put("mail.smtp.user", from);
		      properties.put("mail.smtp.password", pass);
		      properties.put("mail.smtp.port", "587");
		      properties.put("mail.smtp.auth", "true");


		      // Get the default Session object.
		      javax.mail.Session session = javax.mail.Session.getDefaultInstance(properties);

		      try {
		         // Create a default MimeMessage object.
		         MimeMessage message = new MimeMessage(session);

		         // Set From: header field of the header.
		         message.setFrom(new InternetAddress(from));

		         // Set To: header field of the header.
		         message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));

		         // Set Subject: header field
		         String subject, body;
		        
		         subject = "MCS Should Invoice Alert";		        

		         
		         
		         
		         body = "Project:  " + "is ready to bill as of today" ;
		 
		         

		         message.setSubject(subject);

		         // Now set the actual message
		         message.setText(body);

		         // Send message
		         Transport transport = session.getTransport("smtp");
		         transport.connect(host, from, pass);
		         transport.sendMessage(message, message.getAllRecipients());
		         transport.close();
//		         Transport.send(message);
		         System.out.println("Sent message successfully....");
		      }catch (MessagingException mex) {
		         mex.printStackTrace();
		      } catch(Exception e) {
		    	  e.printStackTrace();
		      }
		      
		
		}
		else if(action.equals("sendText")) {
			//System.out.println("SENDING TEXT");

			 String phoneNumber = parameters.get("phoneNumber");
	         String textMessage = parameters.get("message");
	         String phoneCarrier = parameters.get("phoneCarrier");
	         System.out.println("Phone Number: " + phoneNumber);
	         System.out.println("Message: " + textMessage);
	         System.out.println("Phone Carrier: " + phoneCarrier);
			
	         String ATT = "txt.att.net";
	         String VERIZON = "vtext.com";
	         String SPRINT = "messaging.sprintpcs.com";
	         
	         switch("phoneCarrier") {
	         	case "1":
	         		phoneCarrier = ATT;
	         		break;
	         	case "2":
	         		phoneCarrier = VERIZON;
	         		break;
	         	case "3":
	         		phoneCarrier = SPRINT;
	         }
			// Recipient's email ID needs to be mentioned.
		      String to = phoneNumber+"@"+phoneCarrier ;
		      //to = "6106576953@txt.att.net";
		      // Sender's email ID needs to be mentioned
		      String from = "BennettWisner";

		      // Assuming you are sending email from localhost
		      String host = "localhost";

		      // Get system properties
		      Properties properties = System.getProperties();

		      // Setup mail server
		      properties.setProperty("mail.smtp.host", host);


		      // Get the default Session object.
		      javax.mail.Session session = javax.mail.Session.getDefaultInstance(properties);

		      try {
		         // Create a default MimeMessage object.
		         MimeMessage message = new MimeMessage(session);

		         // Set From: header field of the header.
		         message.setFrom(new InternetAddress(from));

		         // Set To: header field of the header.
		         message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));

		         // Set Subject: header field
		         String subject, body;
		        
		        // subject = "Text Test";		        
		         
		         body = textMessage;

		        // message.setSubject(subject);

		         // Now set the actual message
		         message.setText(body);

		         // Send message
		         Transport.send(message);
		         System.out.println("Sent message successfully....");
		      }catch (MessagingException mex) {
		         mex.printStackTrace();
		      }
		} else if (action.equals("getSpecificObjects")) {
			//System.out.println("getting specific objects");
			response = ProjectService.getSpecificAsJson(parameters);
			System.out.println("SPECIFIC OBJECT SIZE = " + response.length());

		}
		else if (action.equals("getEditQueryObjects"))
		{
			//System.out.println("qenum");
			response = ProjectService.getEditEnumsAsJSON();
		}
		else if (action.equals("getProjectToEdit"))
		{
			//System.out.println("getEdit");
			String warehouse = (parameters.get("warehouse"));
			String stage = (parameters.get("stage"));
			String classID = (parameters.get("class"));
			String itemID = (parameters.get("item"));
			String projectID = (parameters.get("id"));
			//add a search by permit
			//String inspections= (parameters.get("inspections"));
			response = QueryService.getProjectToEdit(warehouse, stage, classID, itemID, projectID);
		}
		else if(action.equals("getAllProjects"))
		{
			//System.out.println("getting the projectsssss!");
			response = ProjectService.getAllProjectsAsJson();
			
			System.out.println("SIZE OF GET ALL PROJECTS = " + Integer.toString(response.length()));
		}
		else if(action.equals("getAllWarehouses"))
		{			
			response = ProjectObjectService.getAllAsJsonString("Warehouse");						
		}
		else if(action.equals("getAllProjectsIds"))
		{
			System.out.println("getting the projectsssss!");
			response = ProjectService.getAllProjectsIdsAsJson();
			
			System.out.println("SIZE OF GET ALL PROJECTS = " + Integer.toString(response.length()));
		}
		else if (action.equals("getEditableProject"))
		{
			//System.out.println("getEditableProject");
			String warehouse = (parameters.get("whID"));
			String stage = (parameters.get("stageID"));
			String classID = (parameters.get("classID"));
			String itemID = (parameters.get("itemID"));
			String projectID = (parameters.get("projectID"));
			//String inspections=(parameters.get("inspections"));
			response = QueryService.getProjectToEdit(warehouse, stage, classID, itemID, projectID);
		} else if(action.equals("addNewProject")) {
			//System.out.println("addingNewProject");
			
			try
			{
			Long projID = ProjectService.addNewProject(parameters);
			response = Long.toString(projID);
			}
			catch(ClassNotFoundException | ParseException e) 
			{
				e.printStackTrace();
			}
		} else if(action.equals("editExistingProject")) {
			//System.out.println("editExistingProject");
			
			try {
				Long projID = Long.parseLong(parameters.get("projectID"));		
				ProjectService.editExistingProject(projID, parameters);		
				response = Long.toString(projID);
			} catch (NumberFormatException e) {
				System.out.println("ID retrieval failed");
			} catch(ClassNotFoundException | ParseException e) {
				System.out.println("Some other error!");
			}
		} 
			else if(action.equals("saveEvalRules"))
		{
			//System.out.println("saveEvalRules");
			
			try {
				System.out.println(parameters);
				Long projID = Long.parseLong(parameters.get("projectID"));		
				ProjectService.editEvalProject(projID, parameters);		
				response = Long.toString(projID);
			} catch (NumberFormatException e) {
				System.out.println("ID retrieval failed");
			} catch(ClassNotFoundException | ParseException e) {
				System.out.println("Some other error!");
			}		
		}
		else if(action.equals("autofillPermits")) 
		{
			//System.out.println("autofillPermits");
			
			try {
				Long projID = Long.parseLong(parameters.get("projectID"));		
				ProjectService.autofillPermits(projID, parameters);		
				response = Long.toString(projID);
			} catch (NumberFormatException e) {
				System.out.println("ID retrieval failed");
			} catch(ClassNotFoundException | ParseException e) {
				System.out.println("Some other error!");
			}
		} else if(action.equals("autofillHVAC")) {
			//System.out.println("autofillHVAC");
			
			try {
				Long projID = Long.parseLong(parameters.get("projectID"));		
				ProjectService.autofillHVAC(projID, parameters);		
				response = Long.toString(projID);
			} catch (NumberFormatException e) {
				System.out.println("ID retrieval failed");
			} catch(ClassNotFoundException | ParseException e) {
				System.out.println("Some other error!");
			}
		} else if(action.equals("autofillRefrigeration")) {
			//System.out.println("autofillRefrigeration");
			
			try {
				Long projID = Long.parseLong(parameters.get("projectID"));		
				ProjectService.autofillRefrigeration(projID, parameters);		
				response = Long.toString(projID);
			} catch (NumberFormatException e) {
				System.out.println("ID retrieval failed");
			} catch(ClassNotFoundException | ParseException e) {
				System.out.println("Some other error!");
			}
		} else if(action.equals("autofillProjectClass")) {
			//System.out.println("autofillProjectClass");
			
			try {
				Long projID = Long.parseLong(parameters.get("projectID"));		
				ProjectService.autofillProjectClass(projID, parameters);		
				response = Long.toString(projID);
			} catch (NumberFormatException e) {
				System.out.println("ID retrieval failed");
			} catch(ClassNotFoundException | ParseException e) {
				System.out.println("Some other error!");
			}
		} else if (action.equals("editCloseout")) {
			Long projectID = Long.parseLong(parameters.get("projectID"));
			try
			{
			ProjectService.editCloseout(projectID, parameters);
			}
			catch(ClassNotFoundException | ParseException e) 
			{
				e.printStackTrace();
			}
		}
		else if(action.equals("editPermits"))
		{
			Long projectID = Long.parseLong(parameters.get("projectID"));
			try
			{
			ProjectService.editPermits(projectID, parameters);
			}
			catch(ClassNotFoundException | ParseException e) 
			{
				e.printStackTrace();
			}
		}
		else if(action.equals("editPermitNotes")) {
			Long projectID = Long.parseLong(parameters.get("projectID"));
			try
			{
			ProjectService.editPermitNotes(projectID, parameters);
			}
			catch(ClassNotFoundException | ParseException e) 
			{
				e.printStackTrace();
			}
		}
		else if(action.equals("addCostEstimate"))
		{
			Long projectID = Long.parseLong(parameters.get("projectID"));
			try
			{
			ProjectService.addCostEstimate(projectID, parameters);
			}
			catch(ClassNotFoundException | ParseException e) 
			{
				e.printStackTrace();
			}
		}
		else if(action.equals("editInspections"))
		{
			Long projectID = Long.parseLong(parameters.get("projectID"));
			try
			{
				ProjectService.editInspections(projectID, parameters);
			}
			catch(ClassNotFoundException | ParseException e) 
			{
				e.printStackTrace();
			}
		}
		else if(action.equals("addChangeOrder"))
		{
			boolean successfulAddition = true;

			Long projectID = Long.parseLong(parameters.get("projectID"));
			try
			{
				successfulAddition = ProjectService.addChangeOrder(projectID, parameters);
			}
			catch(ClassNotFoundException | ParseException e)
			{
				successfulAddition = false;
				e.printStackTrace();
			}
			
			if(successfulAddition) 	response = " SUCCESSFULLY ADDED CHANGE ORDER\n";
			else response = " ERROR ADDING CHANGE ORDER\n";
		}
		else if(action.equals("addPendingInvoice"))
		{
			boolean successfulAddition = true;

			Long projectID = Long.parseLong(parameters.get("projectID"));
			try
			{
				successfulAddition = ProjectService.addPendingInvoice(projectID, parameters);
			}
			catch(ClassNotFoundException | ParseException e)
			{
				successfulAddition = false;
				e.printStackTrace();
			}
			
			if(successfulAddition) 	response = " SUCCESSFULLY ADDED CHANGE ORDER\n";
			else response = " ERROR ADDING CHANGE ORDER\n";
		}
		else if(action.equals("addInvoice"))
		{
			boolean successfulAddition = true;

			Long projectID = Long.parseLong(parameters.get("projectID"));
			try
			{
				successfulAddition = ProjectService.addInvoice(projectID, parameters);
			}
			catch(ClassNotFoundException | ParseException e)
			{
				successfulAddition = false;
				e.printStackTrace();
			}
			
			if(successfulAddition) 	response = " SUCCESSFULLY ADDED CHANGE ORDER\n";
			else response = " ERROR ADDING CHANGE ORDER\n";
		}
		else if(action.equals("editChangeOrder"))
		{
			boolean successfulEdit = true;
			
			Long projectID = Long.parseLong(parameters.get("projectID"));
			try
			{
				successfulEdit = ProjectService.editChangeOrder(projectID, parameters);
			}
			catch(ClassNotFoundException | ParseException e)
			{
				successfulEdit = false;
				e.printStackTrace();
			}
			
			if(successfulEdit) response = " SUCCESSFULLY EDITED CHANGE ORDER\n";
			else response = " ERROR EDITING CHANGE ORDER\n";
		}
		else if(action.equals("addEquipment"))
		{
			Long projectID = Long.parseLong(parameters.get("projectID"));
			try
			{
				ProjectService.addEquipment(projectID,  parameters);
			}
			catch(ClassNotFoundException | ParseException e)
			{
				e.printStackTrace();
			}
		}
		else if(action.equals("addProjSpecScope"))
		{
			Long projectID = Long.parseLong(parameters.get("projectID"));
			try
			{
				ProjectService.addProjectSpecScope(projectID,  parameters);
			}
			catch(ClassNotFoundException | ParseException e)
			{
				e.printStackTrace();
			}
		}
		else if(action.equals("addMasterScope"))
		{
			try
			{
				ProjectService.addMasterScope(parameters);
			}
			catch(ClassNotFoundException | ParseException e)
			{
				e.printStackTrace();
			}
		}
		else if(action.equals("addProjMasterScope"))
		{
			try
			{
				ProjectService.addProjMasterScope(parameters);
			}
			catch(ClassNotFoundException | ParseException e)
			{
				e.printStackTrace();
			}
		}
		else if(action.equals("editEquipment"))
		{
			Long projectID = Long.parseLong(parameters.get("projectID"));
			try
			{
				ProjectService.editEquipment(projectID, parameters);
			}
			catch(ClassNotFoundException | ParseException e)
			{
				e.printStackTrace();
			}
		}
		else if (action.equals("get"))
		{
			//System.out.println("GET");
			try 
			{
				response = (String) ProjectObjectService.getAsJSON(Long.parseLong(parameters.get("id")), "Project");
				System.out.println("project: "+response);
			} 
			catch (NumberFormatException | ClassNotFoundException e) {
				e.printStackTrace();
			}
		} 
		else if(action.equals("getWarehouse")) 
		{
			//System.out.println("GET WAREHOUSE");
			try 
			{
				response = (String) ProjectObjectService.getAsJSON(Long.parseLong(parameters.get("id")), "Warehouse");
				System.out.println("project: "+response);
			} 
			catch (NumberFormatException | ClassNotFoundException e) {
				e.printStackTrace();
			}
		} 
		else if(action.equals("getItem")) 
		{
			//System.out.println("GET ITEM");
			try 
			{
				response = (String) ProjectObjectService.getAsJSON(Long.parseLong(parameters.get("id")), "ProjectItem");
				System.out.println("project: "+response);
			} 
			catch (NumberFormatException | ClassNotFoundException e) {
				e.printStackTrace();
			}
		} 
		else if(action.equals("getMasterScopes")) 
		{
			//System.out.println("GET master scopes");
			try 
			{
				response = ProjectObjectService.getMasterScopesAsJSON();
				System.out.println("project: "+response);
			} 
			catch (NumberFormatException e) {
				e.printStackTrace();
			}
		}
		else if(action.equals("getSpecMasterScope")) 
		{
			//System.out.println("GET spec master scopes");
			try 
			{
				response = ProjectObjectService.getSpecMasterScope(Long.parseLong(parameters.get("id")));
				System.out.println("project: "+response);
			} 
			catch (NumberFormatException e) {
				e.printStackTrace();
			}
		}
		else if(action.equals("getSpecProjScope")) 
		{
			//System.out.println("GET spec proj scopes");
			try 
			{
				response = ProjectObjectService.getSpecProjScope(Long.parseLong(parameters.get("id")));
				System.out.println("project: "+response);
			} 
			catch (NumberFormatException e) {
				e.printStackTrace();
			}
		}
		else if(action.equals("getSpecProject")) 
		{
			//System.out.println("GET spec project");
			try 
			{
				response = ProjectObjectService.getSpecProject(Long.parseLong(parameters.get("id")));
				System.out.println("project: "+response);
			} 
			catch (NumberFormatException e) {
				e.printStackTrace();
			}
		}
		else if(action.equals("getSpecProjMasterScope")) 
		{
			//System.out.println("GET spec proj master scopes");
			try 
			{
				response = ProjectObjectService.getSpecProjMasterScope(Long.parseLong(parameters.get("id")));
				System.out.println("project: "+response);
			} 
			catch (NumberFormatException e) {
				e.printStackTrace();
			}
		}
		else if(action.equals("getProjCostEst")) 
		{
			//System.out.println("GET cost est");
			try 
			{
				response = ProjectObjectService.getSpecCostEst(Integer.parseInt(parameters.get("id")));
				System.out.println("project: " + response);
			} 
			catch (NumberFormatException e) {
				e.printStackTrace();
			}
		}
		else if(action.equals("getProjectItem")) 
		{
			//System.out.println("GET spec projectItem");
			try 
			{
				response = ProjectObjectService.getProjItemAsJSON(Long.parseLong(parameters.get("id")));
				System.out.println("project: "+response);
			} 
			catch (NumberFormatException e) {
				e.printStackTrace();
			}
		}
		else if (action.equals("getQueryEnums"))
		{
			response = ProjectService.getQueryEnumsAsJSON();
		}
		else if (action.equals("getAllEquipmentObjects"))
		{
			response = ProjectService.getAllEnumsEquipAsJson();
		}
		else if (action.equals("getAllCities"))
		{
			response = ProjectObjectService.getAllAsJsonString("City");
		}
		else if (action.equals("getAllTrades"))
		{
			response = ProjectObjectService.getAllAsJsonString("Trade");
		}
		else if (action.equals("getAllSubcontractors"))
		{
			response = ProjectObjectService.getAllAsJsonString("Subcontractor");
		}
		// Very aggressive request TODO: Would be great to somehow minify this request
		else if(action.equals("getManager"))
		{
			response = getManager(req);
		}
		else if(action.equals("deleteProject"))
		{
			//System.out.println("deleting a project");
			
			try {
				Gson gson = new Gson();
				response = ProjectService.delete(Long.parseLong(parameters.get("id")), "Project");
				response = gson.toJson(response);
			} catch (NumberFormatException | ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
		else if(action.equals("deleteMasterScope"))
		{
			//System.out.println("deleting a master scope");
			
			try {
				Gson gson = new Gson();
				response = ProjectObjectService.deleteMasterScope(Integer.parseInt(parameters.get("id")));
				response = gson.toJson(response);
			} catch (NumberFormatException | ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
		else if(action.equals("deleteProjSpecScope"))
		{
			//System.out.println("deleting a proj spec scope");
			
			try {
				Gson gson = new Gson();
				response = ProjectObjectService.deleteProjSpecScope(Integer.parseInt(parameters.get("id")));
				response = gson.toJson(response);
			} catch (NumberFormatException | ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
		else if(action.equals("deleteProjectObject")) 
		{
			try {
				Gson gson = new Gson();
				response = ProjectService.delete(Long.parseLong(parameters.get("id")), parameters.get("domain"));
				response = gson.toJson(response);
			} catch (NumberFormatException | ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
		else if(action.equals("deleteChangeOrder"))
		{
			//System.out.println("deleting a change order");
			try {
				ProjectService.removeChangeOrder(Long.parseLong(parameters.get("projectID")), Long.parseLong(parameters.get("changeOrderID")));
				ProjectService.delete(Long.parseLong(parameters.get("changeOrderID")), "ChangeOrder");
				response = "DELETED";
				//PUT BACK CLASS NOT FOUND EXCEPTION
			} catch (NumberFormatException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		else if(action.equals("deleteEquipment"))
		{
			
			//System.out.println("Deleting Equipment");
			
			String equipmentToDelete = parameters.get("equipmentID");
			String associatedProjectID = parameters.get("projectID");
			
			Long equipmentID = Long.parseLong(equipmentToDelete);
			Long projectID = Long.parseLong(associatedProjectID);
			
			try {
				ProjectService.removeEquipment(projectID , equipmentID);
				ProjectService.delete(equipmentID , "NewEquipment");
				response = "DELETED";
				//PUT BACK CLASS NOT FOUND EXCEPTION
			} catch (NumberFormatException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			response = "DELETED EQUIPMENT";
		}
		else if(action.equals("getProjectManagers"))
		{
			//System.out.println("Gettng Names of All Project Managers");
			// TODO: Store User's name under User.class this implementation is really bad
			//String[] projectManagers = {"Adrienne","Alex","Andy", "Bart", "Craig", "Dave",
			//				   "David", "Joe", "Scott"};
			List<Object> projectManagers = ProjectObjectService.getAll("Person");
			// TODO: WHEN PEOPLE HAVE FIRST NAMES response = ProjectObjectService.getAllAsJsonString("User");
			Gson gson = new Gson();
			response = gson.toJson(projectManagers);
			System.out.println("Proj managers = " + response);
		}
		else if (action.equals("getUsers")) {
			//System.out.println("Gettng Names of All Users");
			// TODO: Store User's name under User.class this implementation is really bad
			List<Object> users= ProjectObjectService.getAll("User");
			
			
			// TODO: WHEN PEOPLE HAVE FIRST NAMES response = ProjectObjectService.getAllAsJsonString("User");
			Gson gson = new Gson();
			response = gson.toJson(users);
		} else if (action.equals("getCustomers")) {			
			// TODO: Store User's name under User.class this implementation is really bad
			List<Object> customers= ProjectObjectService.getAll("Customer");
			
			
			// TODO: WHEN PEOPLE HAVE FIRST NAMES response = ProjectObjectService.getAllAsJsonString("User");
			Gson gson = new Gson();
			response = gson.toJson(customers);
		} else if (action.equals("editCustomer")) {			
			
			try
			{
			ProjectObjectService.editCustomer(parameters);
			}
			catch(ClassNotFoundException e) 
			{
				e.printStackTrace();
			}
		} else if (action.equals("createTask")) {
			//System.out.println("Creating Task");
			
			try {
			response = ProjectService.createTask(parameters, (String) req.getSession().getAttribute("user"));
			} catch(ClassNotFoundException | ParseException e) {
				e.printStackTrace();
			}
		}  else if (action.equals("createPendInv")) {
			//System.out.println("Creating PendInv");
			
			try {
			response = ProjectService.createPendInv(parameters, (String) req.getSession().getAttribute("user"));
			} catch(ClassNotFoundException | ParseException e) {
				e.printStackTrace();
			}
			
			
		} else if (action.equals("updatePendInv")) {
			//System.out.println("Updating PendInv");
			
			PendingInvoice currentPendInv = null;			
			try {
				long pendInvID = Long.parseLong(parameters.get("pendInvID"));
				currentPendInv = (PendingInvoice)ProjectObjectService.get(pendInvID,  "PendingInvoice");
			} catch (ClassNotFoundException | NumberFormatException e) {
				e.printStackTrace();
			}

			try {
				PendInvFiller.fillPendInvInformation(currentPendInv, parameters, (String) req.getSession().getAttribute("user"));
			} catch (ClassNotFoundException | ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			Session session = HibernateUtil.getSession();
			Transaction tx = session.beginTransaction();
			session.clear();
			session.update(currentPendInv);
			tx.commit();

			response =  "UPDATED_PENDINV";
			
			/////////////////////////////////////////////////////////////
			//creates invoice
		}  else if (action.equals("createInvoice")) {
			
			try {
			response = ProjectService.createInvoice(parameters, (String) req.getSession().getAttribute("user"));
			} catch(ClassNotFoundException | ParseException e) {
				e.printStackTrace();
			}
			
			//updates invoice
		} else if (action.equals("updateInvoice")) {
			
			Invoice currentInvoice = null;			
			try {
				long invoice_ID = Long.parseLong(parameters.get("currInvoice"));
				currentInvoice = (Invoice)ProjectObjectService.get(invoice_ID,  "Invoice");
			} catch (ClassNotFoundException | NumberFormatException e) {
				e.printStackTrace();
			}

			try {
				InvoiceFiller.fillInvoiceInformation(currentInvoice, parameters, (String) req.getSession().getAttribute("user"));
			} catch (ClassNotFoundException | ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			Session session = HibernateUtil.getSession();
			Transaction tx = session.beginTransaction();
			session.clear();
			session.update(currentInvoice);
			tx.commit();

			response =  "UPDATED_INVOICE";
			
			///////////////////////////////////////////////////////
		} else if (action.equals("getTasks")) {
			try {
				//System.out.println("Getting Tasks");
				response = ProjectObjectService.getAllAsJsonString("Task");

			} catch (NumberFormatException e) {
				e.printStackTrace();
			}

		} else if (action.equals("getTheTasks")) {
			try {
				//System.out.println("Getting Tasks");
				response = ProjectObjectService.getAllAsJsonString("Task");

			} catch (NumberFormatException e) {
				e.printStackTrace();
			}

		} else if (action.equals("getUserInfo")) {
			//System.out.println("getting User Info");
			System.out.println("GET == " + req.getSession().getAttribute("user"));
			System.out.println(User.mapNameToUser((String)req.getSession().getAttribute("user")));
			Gson g = new Gson();
			response = g.toJson(User.mapNameToUser((String) req.getSession().getAttribute("user")));
			System.out.println("response = " +response);
		} else if (action.equals("updateTask")) {
			//System.out.println("Updating Task");
			
			Task currentTask = null;
			try {
				long taskID = Long.parseLong(parameters.get("taskID"));
				currentTask = (Task)ProjectObjectService.get(taskID,  "Task");
			} catch (ClassNotFoundException | NumberFormatException e) {
				e.printStackTrace();
			}

			try {
				TaskFiller.fillTaskInformation(currentTask, parameters, (String) req.getSession().getAttribute("user"));
			} catch (ClassNotFoundException | ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			Session session = HibernateUtil.getSession();
			Transaction tx = session.beginTransaction();
			session.clear();
			session.update(currentTask);
			tx.commit();

			response =  "UPDATED_TASK";
			
		} else if (action.equals("getProjectTasks")) {
			//System.out.println("getting Project tasks");
			response = ProjectObjectService.getProjectTasksAsJSON(Long.parseLong(parameters.get("id")));
		} else if (action.equals("getProjectPendInvs")) {
			//System.out.println("getting Project Pending Invoices");
			response = ProjectObjectService.getProjectPendInvsAsJSON(Long.parseLong(parameters.get("id")));
		} else if (action.equals("getProjectInvs")) {
			//System.out.println("getting Project Pending Invoices");
			response = ProjectObjectService.getProjectInvsAsJSON(Long.parseLong(parameters.get("id")));
		}else if (action.equals("getCompCostEst")) {
			//System.out.println("getting comparable cost ests");
			response = ProjectObjectService.getComparableCostEst(Integer.parseInt(parameters.get("id")));
		} else if (action.equals("getProjSpecScopes")) {
			//System.out.println("getting Project spec scopes");
			response = ProjectObjectService.getProjSpecScopesAsJSON(Long.parseLong(parameters.get("id")));
		} else if (action.equals("closeTask")) {
			//System.out.println("Closing Task");
			try {
				Task task = (Task)ProjectObjectService.get(Long.parseLong(parameters.get("taskID")), "Task");
				
				task.setCompleted(true);
				
				Session session = HibernateUtil.getSession();
				Transaction tx = session.beginTransaction();
				session.clear();
				session.update(task);
				tx.commit();
				response = "TASK_CLOSED";
			} catch (NumberFormatException | ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} 
		else if(action.equals("getSpecificTask")) {
			try {
				//System.out.println("Coming here");
				System.out.println(parameters.get("taskID"));
				System.out.println(Long.parseLong(parameters.get("taskID")));
				//System.out.println("Coming here only");
				Task currentTask = (Task)ProjectObjectService.get(Long.parseLong(parameters.get("taskID")), "Task");				
				String name = currentTask.getAssignee().getFirstName();
				System.out.println("Old name us " + name);
				Gson gson = new Gson();
				response = gson.toJson(name);
				System.out.println(response);
			} catch (NumberFormatException | ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else if (action.equals("closeTask")) {
			//System.out.println("Closing Task");
			try {
				Task task = (Task)ProjectObjectService.get(Long.parseLong(parameters.get("taskID")), "Task");
				
				task.setCompleted(true);
				
				Session session = HibernateUtil.getSession();
				Transaction tx = session.beginTransaction();
				session.clear();
				session.update(task);
				tx.commit();
				response = "TASK_CLOSED";
			} catch (NumberFormatException | ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			//Is this even being used? -Fardeen
		}else if(action.equals("changePassword")){
		
			//System.out.println("Change Password");
			try {
				User user = (User)ProjectObjectService.get(Long.parseLong(parameters.get("id")), "User");
				String pass = parameters.get("newPassword");
				System.out.println("NEW PASSWORD IS ......." + pass);

				
				HashGen hG = new HashGen();
				try{
					pass = hG.getHash(pass);
				}
				catch(Exception e)
				{
					System.out.println("hashing went really wrong!");
					e.printStackTrace();
				}
				finally{
				System.out.println("NEWER PASSWORD IS ......." + pass);
				user.setPassword(pass);
				
				Session session = HibernateUtil.getSession();
				Transaction tx = session.beginTransaction();
				session.clear();
				session.update(user);
				tx.commit();
				//HibernateUtil.closeDownSession();
				response = "USER_UPDATED";
				}
			} catch (NumberFormatException | ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else if(action.equals("getSubcontractors"))
		{
			//System.out.println("Getting Subcontractors");
			
			List<Object> subcontractors = ProjectObjectService.getAll("Subcontractor");
			Gson gson = new Gson();
			response = gson.toJson(subcontractors);
			System.out.println("Subcontractors = " + response);
		}
		else if(action.equals("GET_ALL_PROJECTS"))
		{
			
			//System.out.println("Getting PROJECTS");
			
			String projects = ProjectObjectService.getTheProjects();
			Gson gson = new Gson();
			
			//response = gson.toJson(projects);
			response = projects;
		
			System.out.println("SIZE OF GET ALL PROJECTS = " + response.length());
			
			//System.out.println("Projects = " + response);
		}
		else if(action.equals("GET_ALL_PROJECTS_WITH_RULE"))
		{
			
			//System.out.println("Getting PROJECTS");
			
			String projects = ProjectObjectService.getTheProjectsWithRuleResults();
			Gson gson = new Gson();
			
			//response = gson.toJson(projects);
			response = projects;
		
			System.out.println("SIZE OF GET ALL PROJECTS WITH RULE RESULTS = " + response.length());
			
			//System.out.println("Projects = " + response);
		}
		else if(action.equals("getSpecificFieldIdsOfProject"))
		{
			
			//System.out.println("Getting PROJECT FIELDS FOR TASK");
			
			String projects = ProjectObjectService.getSpecificFieldIdsOfProject(parameters);
			Gson gson = new Gson();
			
			response = projects;
		}
		else if(action.equals("updateScore"))
		{
			projectObjects.Project project = null;
			try 
			{
			 project = (projectObjects.Project) ProjectObjectService.get(Long.parseLong(parameters.get("id")), "Project");
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			
			ProjectObjectService.updateProjectScore(project);
			response = "SCORES UPDATED";
			//System.out.println("updating score");
			
		}
		else if(action.equals("evaluateProject"))
		{
			List<ProjectRule> rules = ProjectObjectService.getAllRules();
			projectObjects.Project project = null;
			try 
			{
			 project = (projectObjects.Project) ProjectObjectService.get(Long.parseLong(parameters.get("projectId")), "Project");
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			
			List<ProjectRule> applicableRules = new ArrayList<ProjectRule>();
			for(ProjectRule rule : rules)
			{
				if(rule.getProjectClass() == null || rule.getProjectClass().getName().equalsIgnoreCase(project.getProjectClass().getName()))
					applicableRules.add(rule);
			}
			
			Map<String , Object> result = ProjectRuleService.EvaluateProject(rules, project);
			result.put("applicableRules", applicableRules);
			
			ProjectObjectService.updateProjectScore(project);
			
			
			Gson gson = new Gson();
			response = gson.toJson(result);
			
		}
		
		else if(action.equals("getScoreRules")) {
			projectObjects.Project project = null;
			projectObjects.Permits permits = null;
			try 
			{
			 project = (projectObjects.Project) ProjectObjectService.get(Long.parseLong(parameters.get("projectId")), "Project");
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}

			System.out.println(project.getPermits().getPermitNotes());
			
			
			
			
			//String task = ProjectObjectService.getProjectTasksAsJSON(Long.parseLong(parameters.get("projectId")));
			
			List<Task> task = ProjectObjectService.getAllTasks(Long.parseLong(parameters.get("projectId")));
			List<ChangeOrder> changeOrders = ProjectObjectService.getAllChangeOrders(Long.parseLong(parameters.get("projectId")));
			List<NewEquipment> equipment = ProjectObjectService.getAllNewEquipment(Long.parseLong(parameters.get("projectId")));
			List<PendingInvoice> pendInvs = ProjectObjectService.getAllPendInvs(Long.parseLong(parameters.get("projectId")));
			List<Invoice> invoice = ProjectObjectService.getAllInvoices(Long.parseLong(parameters.get("projectId")));
			
			System.out.println("equipment size is "+ equipment.size());
			ArrayList<RuleDetails> result = new ArrayList<>();
			result.addAll(ProjectNewRuleService.generalInfoEvaluate(project));
			result.addAll(ProjectNewRuleService.financialEvaluate(project, pendInvs,invoice));
			result.addAll(ProjectNewRuleService.schedulingEvaluate(project));
			result.addAll(ProjectNewRuleService.tasksEvaluate(task));
			result.addAll(ProjectNewRuleService.changeOrdersEvaluate(project, changeOrders));
			result.addAll(ProjectNewRuleService.equipmentEvaluate(equipment));
			result.addAll(ProjectNewRuleService.permitsEvaluate(project));
			result.addAll(ProjectNewRuleService.closeOutEvaluate(project));
			Gson gson = new Gson();
			response = gson.toJson(result);
		}
		else if(action.equals("getScoreColor")) {
			
			 String[] projects = req.getParameterValues("project");
			 System.out.println("projects are "+ projects);
			 String projectsNew = projects[0];
			 projectsNew = projectsNew.substring(1, projectsNew.length()-1);
			 //System.out.println(String.split(projects));
			 System.out.println("new projs are  "+projectsNew);
			 String[] projectsNewSplit = projectsNew.split(",");
			 System.out.println(projectsNewSplit[0]);
			 //String[][] projectsIdColor;
				
				 HashMap<String, String> projectsIdColorHashMap = ProjectNewRuleColorService.generalInfoColor(projectsNewSplit);

				Gson gson = new Gson();
				response = gson.toJson(projectsIdColorHashMap);
			}


			
		
		else if(action.equals("getRules"))
		{
			List<ProjectRule> rules = ProjectObjectService.getAllRules();
			projectObjects.Project project = null;
			try 
			{
			 project = (projectObjects.Project) ProjectObjectService.get(Long.parseLong(parameters.get("projectId")), "Project");
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			
			List<ProjectRule> applicableRules = new ArrayList<ProjectRule>();
			for(ProjectRule rule : rules)
			{
				if(rule.getProjectClass() == null || rule.getProjectClass().getName().equalsIgnoreCase(project.getProjectClass().getName()))
					applicableRules.add(rule);
			}
			
			Map<String , Object> result = ProjectRuleService.getScores(rules, project);
			result.put("applicableRules", applicableRules);
			
			Gson gson = new Gson();
			response = gson.toJson(result);
			System.out.println(response);
			
		}
//		else if(action.equals("TESTevaluateProject"))
//		{
//			ProjectRule rule = new ProjectRule(RuleDomain.Tasks , null , null , RuleResult.TASK_ONTIME , RuleSeverity.MEDIUM , 
//					null , null , "Fail Message" , "Pass Message" , "My Title");
//			ProjectRule rule2 = new ProjectRule(RuleDomain.PermitsAndInspections , "voltagePermitStatus" , "voltageInspectionStatus" , RuleResult.SS_NV, 
//					RuleSeverity.MEDIUM , null , null , "Fail Message 2" , "Pass Message 2" , "My Title 2");
//			ProjectRule rule3 = new ProjectRule(RuleDomain.Scheduling , "budgetaryDue" , "budgetarySubmitted" , RuleResult.DD_NV, 
//					RuleSeverity.MEDIUM , null , null , "Fail Message 3" , "Pass Message 3" , "My Title 3");
//			ProjectRule rule4 = new ProjectRule(RuleDomain.Financial , "shouldInvoice" , "actualInvoice" , RuleResult.NN_EQUAL, 
//					RuleSeverity.MEDIUM , null , null , "Fail Message 4" , "Pass Message 4" , "My Title 4");
//			ProjectRule rule5 = new ProjectRule(RuleDomain.Equipment , "orderedDate" , "deliveryDate" , RuleResult.DD_EARLY, 
//					RuleSeverity.MEDIUM , null , null , "Fail Message 5" , "Pass Message 5" , "My Title 5");
//			ProjectRule rule6 = new ProjectRule(RuleDomain.ChangeOrders , "cost" , "sell" , RuleResult.NN_LESS, 
//					RuleSeverity.MEDIUM , null , null , "Fail Message 6" , "Pass Message 6" , "My Title 6");
//			ProjectRule rule7 = new ProjectRule(RuleDomain.Closeout , "mechanicalStatus" , null , RuleResult.CLOSEOUT_NULL_STATUS, 
//					RuleSeverity.MEDIUM , null , null , "Fail Message 7" , "Pass Message 7" , "My Title 7");
//			
//			
//			projectObjects.Project project = null;
//			try 
//			{
//			 project = (projectObjects.Project) ProjectObjectService.get((long) 2119 , "Project");
//			}
//			catch(Exception e)
//			{
//				e.printStackTrace();
//			}
//			
//			List<ProjectRule> list = new ArrayList<ProjectRule>();
//			list = ProjectObjectService.getAllRules();
//			/*
//			list.add(rule);
//			list.add(rule2);
//			list.add(rule3);
//			list.add(rule4);
//			list.add(rule5);
//			list.add(rule6);
//			list.add(rule7);
//			*/
//			
//			Map<String , Object> result = ProjectRuleService.EvaluateProject(list, project);
//			
//			ProjectObjectService.updateProjectScore(project);
//			Gson gson = new Gson();
//			response = gson.toJson(result);
//		}
		else if(action.equals("evaluateAllProjects"))
		{
			List<ProjectRule> rules = ProjectObjectService.getAllRules();

			List<projectObjects.Project> projects = ProjectObjectService.getAllRawProjects();
			
			Map<String , Object> allResults = new HashMap<String , Object>();
			for(projectObjects.Project project : projects)
			{
				Map<String , Object> result = ProjectRuleService.EvaluateProject(rules, project);
				allResults.put(project.getId().toString() , result);
			}
			
			for(projectObjects.Project project : projects)
			{
				ProjectObjectService.updateProjectScore(project);
			}

			
			
			Gson gson = new Gson();
			response = gson.toJson(allResults);
		}
		else if(action.equals("countChangeOrders")) {
			Long projectID = Long.parseLong(parameters.get("projectID"));
			try
			{
			ProjectService.countChangeOrders(projectID);
			}
			catch(ClassNotFoundException | ParseException e) 
			{
				e.printStackTrace();
			}
		}
//		else if(action.equals("TESTevaluateAllProjects"))
//		{
//			ProjectRule rule = new ProjectRule(RuleDomain.Tasks , null , null , RuleResult.TASK_ONTIME , RuleSeverity.MEDIUM , 
//					null , null , "Fail Message" , "Pass Message" , "My Title");
//			ProjectRule rule2 = new ProjectRule(RuleDomain.PermitsAndInspections , "voltagePermitStatus" , "voltageInspectionStatus" , RuleResult.SS_NV, 
//					RuleSeverity.MEDIUM , null , null , "Fail Message 2" , "Pass Message 2" , "My Title 2");
//			ProjectRule rule3 = new ProjectRule(RuleDomain.Scheduling , "budgetaryDue" , "budgetarySubmitted" , RuleResult.DD_NV, 
//					RuleSeverity.MEDIUM , null , null , "Fail Message 3" , "Pass Message 3" , "My Title 3");
//			ProjectRule rule4 = new ProjectRule(RuleDomain.Financial , "shouldInvoice" , "actualInvoice" , RuleResult.NN_EQUAL, 
//					RuleSeverity.MEDIUM , null , null , "Fail Message 4" , "Pass Message 4" , "My Title 4");
//			ProjectRule rule5 = new ProjectRule(RuleDomain.Equipment , "orderedDate" , "deliveryDate" , RuleResult.DD_EARLY, 
//					RuleSeverity.MEDIUM , null , null , "Fail Message 5" , "Pass Message 5" , "My Title 5");
//			ProjectRule rule6 = new ProjectRule(RuleDomain.ChangeOrders , "cost" , "sell" , RuleResult.NN_LESS, 
//					RuleSeverity.MEDIUM , null , null , "Fail Message 6" , "Pass Message 6" , "My Title 6");
//			ProjectRule rule7 = new ProjectRule(RuleDomain.Closeout , "mechanicalStatus" , null , RuleResult.CLOSEOUT_NULL_STATUS, 
//					RuleSeverity.MEDIUM , null , null , "Fail Message 7" , "Pass Message 7" , "My Title 7");
//			
//			
//			List<ProjectRule> list = new ArrayList<ProjectRule>();
//			list = ProjectObjectService.getAllRules();
//			/*
//			list.add(rule);
//			list.add(rule2);
//			list.add(rule3);
//			list.add(rule4);
//			list.add(rule5);
//			list.add(rule6);
//			list.add(rule7);
//			*/
//			
//			List<projectObjects.Project> projects = ProjectObjectService.getAllRawProjects();
//			
//			Map<String , Object> allResults = new HashMap<String , Object>();
//			for(projectObjects.Project project : projects)
//			{
//				Map<String , Object> result = ProjectRuleService.EvaluateProject(list, project);
//				allResults.put(project.getId().toString() , result);
//			}
//			
//			ProjectObjectService.updateProjectScores(projects);
//
//			
//			
//			Gson gson = new Gson();
//			response = gson.toJson(allResults);
//		}

		
		/*
		if(!(action.equals("getAllProjects") || action.equals("getTasks") ||
				action.equals("getSpecificObjects") || action.equals("getAllObjects") || action.equals("getQueryEnums")))
			System.out.println("ACTION = " + action + "\nRESPONSE = " + response);
			*/
		if(response.length() <= 750)
			System.out.println("ACTION = " + action + "\nRESPONSE = " + response);
		else System.out.println("ACTION = " + action);


		
			out = resp.getWriter();
			out.println(response);
		
	}

	/**
	 * @param req
	 * @return
	 */
	private String getManager(HttpServletRequest req) 
	{
		String username = (String) req.getSession().getAttribute("user");
		/* UNDEFINED USERS: Alex, Tony, Jim, Craig*/
		if(username.equals("andy"))
			username = "Andy";
		else if(username.equals("joe"))
			username = "Joe";
		else if(username.equals("bua"))
			username = "Bua";
		else if(username.equals("bart"))
			username ="Bart";
		else if(username.equals("dwgregory1"))
			username = "David";
		else if(username.equals("dschoener"))
			username = "Dave";
		else if(username.equals("alex"))
			username = "Alex";
		else if(username.equals("jim"))
			username = "Jim";
		else if(username.equals("craig"))
			username = "Craig";
		else if(username.equals("tony"))
			username = "Tony";
		else if(username.equals("Marvin"))
			username = "Marvin";
		else if(username.equalsIgnoreCase("scott"))
			username = "Scott";
		else username = "";
		Gson gson = new Gson();
		
		return gson.toJson(username);
	}
	
}

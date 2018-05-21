let AUTO_FILL_OBJECT = {
		PERMITS : false ,
		INSPECTIONS : false ,
		HVAC : false ,
		REFRIGERATION : false ,
};

//$(document).ready(function(){$('#closeoutDocuments').load(function(){	
//
//	autofillCloseoutDocs();
//
//})});

$(document).ready(function(){$('#autofill-HVAC').change(function(){
	if($(this).val() == "default") return;
	
	AUTO_FILL_OBJECT.HVAC = true;
	autofillHVAC();
})});

$(document).ready(function(){$('#autofill-Refrigeration').change(function(){
	if($(this).val() == "default") return;
	
	AUTO_FILL_OBJECT.REFRIGERATION = true;
	autofillRefrigeration();
})});

$(document).ready(function(){$('#autofill-Permits').change(function(){
	if($(this).val() == "default") return;
	
	AUTO_FILL_OBJECT.PERMITS = true;
	AUTO_FILL_OBJECT.INSPECTIONS = true;

	autofillPermits();
	autofillInspections();
})});


let TODAYS_DATE = getTodaysDate();

function autofillHVAC()
{
	console.log("Autofill HVAC");
	let required = $('#autofill-HVAC').val();
	if(required == "default") return;
	
	let REQUIRED = 4; //Correspond to id in database for closeoutstatus (Required)
	let NA = 3; //Correspond to id in database for closeoutstatus (N/A)
	
	let value;
	if(required == 0)
		value = NA;
	else
		value = REQUIRED;
	
	$('.autofill-HVAC').each(function(index){
		$(this).val(value);
	});
	
	$('.autofill-HVAC-date').each(function(index){
		$(this).val(TODAYS_DATE);
	});
	
}

function autofillRefrigeration()
{
	console.log("Autofill Ref");
	
	let required = $('#autofill-Refrigeration').val();
	if(required == "default") return;
	
	let REQUIRED = 4; //Correspond to id in database for closeoutstatus (Required)
	let NA = 3; //Correspond to id in database for closeoutstatus (N/A)
		
	let value;
	if(required == 0)
		value = NA;
	else
		value = REQUIRED;
	
	$('.autofill-Refrigeration').each(function(index){
		$(this).val(value);
	});
	
	$('.autofill-Refrigeration-date').each(function(index){
		$(this).val(TODAYS_DATE);
	});
	
	let html;
	if(required == 0) {
		value = -1;
		html = "N/A";
	}
	else {
		value = 0;
		html = 0;
	}
	
	$('.autofill-Refrigeration-num').each(function(index){
		$(this).val(value);
		$(this).html(html);
	});
	
}

//function autofillCloseoutDocs()
//{
//   let REQUIRED = "4"; 
//
//   let value = REQUIRED; 
//   
//   $('.autofill-CloseoutDocs').each(function(index){
//		$(this).val(value);
//	});
//   
//   $('.autofill-CloseoutDocs-Date').each(function(index){
//		$(this).val(TODAYS_DATE);
//	});
//}

function autofillPermits()
{
	console.log("Autofill Perm");

	let required = $('#autofill-Permits').val();
    
	
	let PREPARING = "Preparing"; //Correspond to id in database for closeoutstatus (Required)
	let NA = "N/A"; //Correspond to id in database for closeoutstatus (N/A)
	let TBD_STATUS = "TBD";
	
	let YES = 0;
	let NO = 1;
	let TBD = 2;
		
	let permitStatusRequirementValue;
	let permitStatusValue;
	let permitTableReq;
	
	if(required == YES) 
	{
		permitStatusValue = PREPARING;
		permitStatusRequirementValue = YES;
		permitTableReq = "Yes";
	} 
	else if(required == NO) 
	{
		permitStatusValue = NA;
		permitStatusRequirementValue = NO;
		permitTableReq = "No";
	} 
	else 
	{
		permitStatusValue = TBD_STATUS;
		permitStatusRequirementValue = TBD;
		permitTableReq = "TBD";
	}
	
	/*
	$('.autofill-Permit-Requirement').each(function(index){
		$(this).val(permitStatusRequirementValue);
	});
	*/
	
	$('.permitReq').each(function(index){
		$(this).html(permitTableReq);
	});
	
	$('.autofill-Permit-Status').each(function(index){
		$(this).val(permitStatusValue);
	});
	
	$('.autofill-Permit-date').each(function(index){
		$(this).val(TODAYS_DATE);
	});
	
}

function autofillInspections()
{
	console.log("Autofill Inspections");

	let required = $('#autofill-Permits').val();
	
	
	let PREPARING = "Preparing"; //Correspond to id in database for closeoutstatus (Required)
	let NA = "N/A"; //Correspond to id in database for closeoutstatus (N/A)
	let TBD_STATUS = "TBD";
	
	let YES = 0;
	let NO = 1;
	let TBD = 2;
		
	let inspectionStatusRequirementValue;
	let inspectionStatusValue;
	let inspectionTableReq;
	
	if(required == YES) 
	{
		inspectionStatusValue = PREPARING;
		inspectionStatusRequirementValue = YES;
		inspectionTableReq = "Yes";
	} 
	else if(required == NO) 
	{
		inspectionStatusValue = NA;
		inspectionStatusRequirementValue = NO;
		inspectionTableReq = "No";
	} 
	else
    {
	    inspectionStatusValue = TBD_STATUS;
	    inspectionRequirementValue = TBD;
	    inspectionTableReq = "TBD";
    }
	
	/*
	$('.autofill-Inspection-Requirement').each(function(index){
		$(this).val(inspectionStatusRequirementValue);
	});
	*/
	
	$('.inspectionReq').each(function(index){
		$(this).html(inspectionTableReq);
	});
	
	$('.autofill-Inspection-Status').each(function(index){
		$(this).val(inspectionStatusValue);
	});
	
	$('.autofill-Inspection-date').each(function(index){
		console.log("THIS" , $(this).val() , TODAYS_DATE);
		$(this).val(TODAYS_DATE);
	});
	

	
}
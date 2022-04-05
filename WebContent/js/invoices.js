let invs;

//gets list of invoices for project
function getInvs(stopServerCalls) {
		
	$.ajax({
		type: 'POST',
		url: 'Project',
		data: {
			'domain': 'project',
			'action': 'getProjectInvs',
			'id': -1
		}, success: function (data) {
			console.log("proj Invoices!!!!", data);
			
			invs = data;
			if (data) {
				clearInvoiceTable();

				fillInvsTable(data);
			}
			if(!stopServerCalls) getUserData();

		}, error: function (data) {
			alert('Server Error!10 INVOICES');
		}
	});	
}


//This enables the user to filter invoices based off status
$(document).on('change', '#invoiceSelector2', function () {	
	clearInvoiceTable();
	fillInvsTable(invs);
});


//fills invoice table 
function fillInvsTable(data) {
	
	let selector = $('#invoiceSelector2').val();	
		
	clearInvoiceTable();
	let invs = data;
	let count = 0;
	
	//shows invoices based on filter selection
	for (var i = 0; i < invs.length; i++) {
		if(	(selector == 'all' && (invs[i].invoiceStatus != "Requested" && invs[i].invoiceStatus != "Processing" && 
				invs[i].invoiceStatus != "Review" && invs[i].invoiceStatus != "Approved")) ||
				(selector == 'requested' && invs[i].invoiceStatus != "Requested") || 
				(selector == 'processing' && invs[i].invoiceStatus != "Processing") ||
				(selector == 'review' && invs[i].invoiceStatus == "Review") ||
				(selector == 'approved' && invs[i].invoiceStatus != "Approved") ||
				(selector == 'submitted' && invs[i].invoiceStatus != "Submitted") ||
				(selector == 'rejected' && invs[i].invoiceStatus != "Rejected")) 
				continue; // do nothing
				
		var inv = invs[i];
		
		var invListing = document.createElement('tr');
		invListing.setAttribute("value", inv);
		
		//allows invoice to be edited
		invListing.onclick = function() {
			toggleInv(this);
		};
		
		invListing.ondblclick = function(){

			editSelectedInv(this);
		};
				
		count++;
		
		invListing.value = invs[i].id;
		invListing.id = "Inv_" + invs[i].id;
		
		let invoiceID = document.createElement('td');
		let associatedPE = document.createElement('td');
		let invoiceTitle = document.createElement('td');
		//let invoiceType = document.createElement('td');		
		let invoiceAmount = document.createElement('td');
		let submittedDate = document.createElement('td');
		let submitRejectDate = document.createElement('td');
		let invoiceCustomer = document.createElement('td');
		let invoiceApproval = document.createElement('td');
		let invoiceStatus = document.createElement('td');
		let invoiceNumber = document.createElement('td');
		let notes = document.createElement('td');
		
		invoiceID.innerHTML = invs[i].associatedPE + '-' + (i+1);
		invoiceTitle.innerHTML = invs[i].invoiceTitle;
		invoiceNumber.innerHTML = invs[i].invoiceNumber;
		//invoiceType.innerHTML = invs[i].invoiceType;
		
		if(invs[i].invoiceAmount)
			invoiceAmount.innerHTML = '$' + invs[i].invoiceAmount;
		else
			invoiceAmount.innerHTML = "---";		
		
		submittedDate.innerHTML = invs[i].submittedDate;
		
		if(invs[i].submitRejectDate)
			submitRejectDate.innerHTML = invs[i].submitRejectDate;
		else
			submitRejectDate.innerHTML = "TBD";
		
		invoiceCustomer.innerHTML = invs[i].invoiceCustomer;
		
		invoiceStatus.innerHTML = invs[i].invoiceStatus;
		invoiceApproval.innerHTML = invs[i].invoiceApproval;
		
		associatedPE.innerHTML = invs[i].associatedPE;
		notes.innerHTML = invs[i].notes;
		
		//invListing.appendChild(associatedPE);
		invListing.appendChild(invoiceID);
		//invListing.appendChild(invoiceTitle);
		//invListing.appendChild(invoiceType);
		invListing.appendChild(invoiceAmount);
		invListing.appendChild(submittedDate);
		invListing.appendChild(submitRejectDate);
		invListing.appendChild(invoiceCustomer);
		//invListing.appendChild(invoiceApproval);
		invListing.appendChild(invoiceStatus);
		invListing.appendChild(invoiceNumber);
		invListing.appendChild(notes);

		$('#invoiceTable > tbody').append(invListing);
		
	}
	//This adds No Invoices to Show to the table if there are none in that filter or category (Ex: Open, Complete)
	if (count === 0) {
		clearAndAddSingleRowInvs("No Invoices to Show");		
	}		
}

function clearInvoiceTable () {
	$('#invoiceTable').find('tr:not(.head)').remove();
}

function clearAndAddSingleRowInvs(msg) {
	$('#invoiceTable > tbody').children('tr:not(.head)').remove();
	
	let placeHolder = document.createElement('tr');
	let listDetails0 = document.createElement('td');
	let listDetails1 = document.createElement('td');
	let listDetails2 = document.createElement('td');	
	let listDetails3 = document.createElement('td');
	let listDetails4 = document.createElement('td');
	let listDetails5 = document.createElement('td');
	let listDetails6 = document.createElement('td');
	let listDetails7 = document.createElement('td');
	let listDetails9 = document.createElement('td');
	let listDetails8 = document.createElement('td');

	listDetails0.innerHTML = msg;
	
	$(placeHolder).append(listDetails0);
	$(placeHolder).append(listDetails1);
	$(placeHolder).append(listDetails2);
	$(placeHolder).append(listDetails3);
	$(placeHolder).append(listDetails4);
	$(placeHolder).append(listDetails5);
	$(placeHolder).append(listDetails6);
	$(placeHolder).append(listDetails7);
	$(placeHolder).append(listDetails8);
	$(placeHolder).append(listDetails9);
	
	$('#invoiceTable > tbody').append(placeHolder);
}

function toggleInv (source) {
	$(source).siblings().css('background-color', 'white');
	$(source).css('background-color', '#dddddd');
	$('#editInvoice').prop('disabled', false);
	editSelectedInv(source);
	
}

//allows user to edit invoice
function editSelectedInv(source)
{
	INV_ACTION = "updateInv";
	clearInvForm();
	displayInvWell();
	fillInvWell(source);
	hideExtraValues();
}

//clears invoice form
function clearInvForm()
{
	$('#invoiceCreationZone').find('#invoiceID').val('');
	$('#invoiceCreationZone').find('#associatedPE').val('');
	$('#invoiceCreationZone').find('#invoiceTitle').val('');
	$('#invoiceCreationZone').find('#invoiceNumber').val('');
	$('#invoiceCreationZone').find('#invoiceType').val('');
	$('#invoiceCreationZone').find('#invoiceStatus').val('Requested');
	$('#invoiceCreationZone').find('#submittedDateInv').val('');
	$('#invoiceCreationZone').find('#invoiceAmount').val('');
	$('#invoiceCreationZone').find('#invoiceCustomer').val('');		
	$('#invoiceCreationZone').find('#invoiceApproval').val('');	
	$('#invoiceCreationZone').find('#notes').val('');	
	$('#invoiceCreationZone').find('#invTotal').val('');
	$('#invoiceCreationZone').find('#currInvoiced').val('');
}

function displayInvWell() {

	document.querySelector('.bg-modal').style.display = "flex";
}

function fillInvWell(source) {
	
	clearInvForm();
	let tmp_id = source;	
	let selected_Inv;
		
	for(var i = 0; i < invs.length; i++) {
		
		//compares all invoices to find the one selected
		if("Inv_"+invs[i].id == tmp_id.id) {
			
			selected_Inv = invs[i];
			INVOICES_STORED = invs[i].id;
		
			break;
		}	
	}
	
	if(!selected_Inv) {
		console.log("IMPROPER INV SELECTION");
		return;
	}	
		
	$('#invoiceCreationZone').find('#invoiceID').val(selected_Inv.invoiceID);
	$('#invoiceCreationZone').find('#associatedPE').val(selected_Inv.associatedPE);
	$('#invoiceCreationZone').find('#invoiceNumber').val(selected_Inv.invoiceNumber);
	$('#invoiceCreationZone').find('#invoiceTitle').val(selected_Inv.invoiceTitle);
	$('#invoiceCreationZone').find('#invoiceType').val(selected_Inv.invoiceType);
	$('#invoiceCreationZone').find('#submittedDateInv').val(selected_Inv.submittedDate);
	$('#invoiceCreationZone').find('#invoiceCustomer').val(selected_Inv.invoiceCustomer);
	$('#invoiceCreationZone').find('#invoiceStatus').val(selected_Inv.invoiceStatus);
	$('#invoiceCreationZone').find('#invoiceApproval').val(selected_Inv.invoiceApproval);	
	$('#invoiceCreationZone').find('#submitRejectDate').val(selected_Inv.submitRejectDate);
	$('#invoiceCreationZone').find('#notes').val(selected_Inv.notes);	
	$('#invoiceCreationZone').find('#projID').val(selected_Inv.invoice_id);	
	$('#invoiceCreationZone').find('#invoiceAmount').val(selected_Inv.invoiceAmount);


			
	/*
	var invoiced = Number(0);
	
	//sums up all invoices for PE
	for(var j=0; j < invs.length; j++){
		
		if(SELECTED_PE_ID.mcsCO == invs[j].associatedPE && invs[j].invoiceStatus != "Rejected"){
			
			invoiced = invoiced + Number(invs[j].invoiceAmount);
		}
	}
	
	var balance = Number(SELECTED_PE_ID.sell) - Number(invoiced);
	
	//these values with dollar amounts are put at bottom as not to intefere with invoice calculation
	$('#invoiceInformation').find('#invoiceCreationZone').find('#invoiceBalance').val(cleanNumericValueForDisplaying(balance));
	$('#invoiceCreationZone').find('#invoiceAmount').val(cleanNumericValueForDisplaying(selected_Inv.invoiceAmount));
	$('#invoiceInformation').find('#invoiceCreationZone').find('#invTotal').val(cleanNumericValueForDisplaying(SELECTED_PE_ID.sell));
*/
}

//prompts user when they want to back out of invoice
function viewInv() {
	
	let updateMessage = "These changes will not be saved, are you sure you want to leave the screen?";
	let createMessage = "This Invoice will not be added, are you sure you want to leave this screen?";
	let displayedMessage;
	
	if(INV_ACTION == "createInv")
		displayedMessage = createMessage;
	else 
		displayedMessage = updateMessage;
	
	if(confirm(displayedMessage))
	{
		//document.getElementById('invoiceInformation').style.width = "100%";
		$('#invoiceCreationZone').hide();
		$('#invoiceDisplay').show();
		//$('#returnAccountsReceivable').show();
	}
	
	$('#invoiceCreationZone').find('#percentOrAmountRow').hide();
	
	$('#invoiceCreationZone').find('#invoiceStatusSelectionRow').show();
}

function hideExtraValues(){
	
	$('#invoiceCreationZone').find('#invoiceStatusSelectionRow').show();

	$('#invoiceCreationZone').find('#percentOrAmountRow').hide();
	$('#invoiceCreationZone').find('#invoiceAmountHide').hide();
	$('#invoiceCreationZone').find('#hide1').hide();
	$('#invoiceCreationZone').find('#hide2').hide();
	$('#invoiceCreationZone').find('#hide3').hide();
	$('#invoiceCreationZone').find('#hide4').hide();
	$('#invoiceCreationZone').find('#hide5').hide();
	$('#invoiceCreationZone').find('#hide6').hide();
	$('#invoiceCreationZone').find('#hide7').hide();
	$('#invoiceCreationZone').find('#hide8').hide();
	$('#invoiceCreationZone').find('#hide10').hide();
	$('#invoiceCreationZone').find('#hide11').hide();
	$('#invoiceCreationZone').find('#hide12').hide();


}

function submitInv() {
	let invoiceID = $('#invoiceCreationZone').find('#invoiceID').val();
	let associatedPE = $('#invoiceCreationZone').find('#associatedPE').val();
	let invoiceTitle = $('#invoiceCreationZone').find('#invoiceTitle').val();
	let invoiceNumber = $('#invoiceCreationZone').find('#invoiceNumber').val();
	let invoiceType = $('#invoiceCreationZone').find('#invoiceType').val();

	let invoice_id = $('#invoiceCreationZone').find('#invoiceID').val();
	
	let projectID = $('#invoiceCreationZone').find('#projID').val();

	let amountType = $('#invoiceCreationZone').find('#percentOrAmount').val();	

	let submittedDate = $('#invoiceCreationZone').find('#submittedDateInv').val();		
	let submitRejectDate = $('#invoiceCreationZone').find('#submitRejectDate').val();		
	let invoiceAmount = $('#invoiceCreationZone').find('#invoiceAmount').val();
	let invoiceCustomer = $('#invoiceCreationZone').find('#invoiceCustomer').val();
	let invoiceStatus = $('#invoiceCreationZone').find('#invoiceStatus').val();
	let invoiceApproval = $('#invoiceCreationZone').find('#invoiceApproval').val();
	let notes = $('#invoiceCreationZone').find('#notes').val();
		
	if (typeof projectID === 'undefined') return alert("Project ID Failed. Find Another Project");

	
	if(INV_ACTION == "updateInv"){
		$.ajax({
			type: 'POST',
			url: 'Project', 
			data: {
				
				'action': 'updateInvoice',
				'project': projectID,
				'currInvoice':INVOICES_STORED,
				'invoiceID': invoiceID,
				'associatedPE': associatedPE,
				'invoiceTitle': invoiceTitle,
				'invoiceNumber': invoiceNumber,
				'invoiceType': invoiceType,
				'submittedDate': submittedDate,
				'submitRejectDate':submitRejectDate,
				'invoiceAmount': invoiceAmount,
				'invoiceCustomer': invoiceCustomer,
				'invoiceStatus': invoiceStatus,
				'invoiceApproval' : invoiceApproval,				
				'notes' : notes,
				'invoice_id' : invoice_id,
																																																				
			}, complete: function (serverResponse) {
				console.log(serverResponse);
				let response = $.trim(serverResponse.responseText);
				if (response === 'UPDATED_INVOICE') {
					alert('Invoice Updated Successfully');
				
					//Makes the user return to the invoice screen 
					//document.getElementById('invoiceInformation').style.width = "100%";
					$('#invoiceCreationZone').hide();
					$('#invoiceDisplay').show();
					clearInvoiceTable();
					getInvs(1);
				}
			}
		});
	}	
}


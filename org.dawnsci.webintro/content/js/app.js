function javaReady(){
	updateData();
}

function updateData(){
	$( "#title span" ).first().show( "slow", function showNext() {
		$( this ).next( "#title span" ).show( "slow", showNext );
	});
	$("#data-here").hide(400);
	data = $.parseJSON(java.getActionsJSON());
	data.actions.forEach(function(entry){
		$("#data-here").append(entry.name+"<br>");
	});
	//$("#data-here").html(java.getActionsJSON());
	$("#data-here").slideDown(3000);
}

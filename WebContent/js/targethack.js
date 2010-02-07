// The XHTML standard forbids using 'target="_blank"' despite this being the
// most accessible way to open new windows (it requires no Javascript and
// popup blockers typically don't stop it).
//
// Therefore this javascript function rewrites every page using DOM such that
// any link with a 'rel' value of 'external' has its 'target' set to '_blank'.
//
// Unfortunately, the 'rel' value is not allowed on forms!  Fortunately these
// forms are never currently referenced in Javascript, so they have been given
// an id of 'target_hack' to be identified by this script.
// 
// I know this is ugly and underhanded, but I honestly feel it's a fair trade
// between usability and accessibility.

function externalLinks() {
	// Check if the DOM is supported.
	if (!document.getElementsByTagName) {
		return;
	}

	// Iterate through the anchors.
	var anchors = document.getElementsByTagName("a");
	for ( var i = 0; i < anchors.length; i++) {
		var anchor = anchors[i];
		
		// If the href is set and the rel is external set the target to _blank
		if (anchor.getAttribute("href")
				&& anchor.getAttribute("rel") == "external") {
			anchor.target = "_blank";
		}
	}

	// Iterate through the forms
	var forms = document.getElementsByTagName("form");
	for ( var i = 0; i < forms.length; i++) {
		var forms = anchors[i];
		
		// If the action is set and the id is target_hack set the target to _blank
		if (forms.getAttribute("action")
				&& forms.getAttribute("id") == "target_hack") {
			forms.target = "_blank";
		}
	}
}

window.onload = externalLinks;
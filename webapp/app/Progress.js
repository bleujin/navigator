(function ($, window) {

	function Progress() {

		var opts = {
			lines    : 13, // The number of lines to draw
			length   : 11, // The length of each line
			width    : 5, // The line thickness
			radius   : 17, // The radius of the inner circle
			corners  : 1, // Corner roundness (0..1)
			rotate   : 0, // The rotation offset
			color    : '#FFF', // #rgb or #rrggbb
			speed    : 1, // Rounds per second
			trail    : 60, // Afterglow percentage
			shadow   : false, // Whether to render a shadow
			hwaccel  : false, // Whether to use hardware acceleration
			className: 'spinner', // The CSS class to assign to the spinner
			zIndex   : 2e9, // The z-index (defaults to 2000000000)
			top      : 'auto', // Top position relative to parent in px
			left     : 'auto' // Left position relative to parent in px
		};

		var target = document.createElement("div");

		document.body.appendChild(target);

		this.spinner = new Spinner(opts).spin(target),
		this.overlay = null;

		this.show = function (message, duration) {
			this.overlay = iosOverlay({
				text    : message || "Loading",
				duration: duration || 2e3,
				spinner : this.spinner
			});
		},
		this.success = function (message, duration) {
			this.overlay = iosOverlay({
				text    : message || "Success",
				duration: duration || 2e3,
				icon: '/app/lib/iosOverlay/check.png'
			});
		},
		this.fail = function (message, duration) {
			this.overlay = iosOverlay({
				text    : message || "Failed",
				duration: duration || 2e3,
				icon: '/app/lib/iosOverlay/cross.png'
			});
		},
		this.hide = function () {
			if(this.overlay !== null) {
				this.overlay.hide();
			}
		}
	}

	window.Progress = Progress;

})(jQuery, window);
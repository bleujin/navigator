(function ($, window) {

	function ReferenceDialog(Stage) {

		var _self = this;

		var updateConnection = function(label) {

			NC.Progress.show('Creating nodes...', 99999999);

			Stage.updateConnection(_self.currentConnection, label);

			setTimeout(function() {
				NC.Progress.hide();
				NC.Progress.success("Success", 1000);
			}, 2000);

		}

		var connEditorPanel = $('#connEditorPanel');

		$('#connEditorPanel .action_button').on('click', function() {
			var label = $('#connName').val();

			updateConnection(label);

			connEditorPanel.modal('hide');
		});

		this.currentConnection = null;

		this.show = function(connection) {

			this.currentConnection = connection;

			connEditorPanel.modal('show');
		};

	}

	window.ReferenceDialog = ReferenceDialog;

})(jQuery, window);
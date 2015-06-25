(function ($, window) {

	function NodeNameDialog(Stage) {

		var _self = this;

		var nodeNameEditorPanel = $('#nodeNameEditorPanel');

		var callback = null;

		this.parentNodePath = null;
		this.originalId = null;

		var saveNode = function(nodeName) {
			var nodeFullPath = (_self.parentNodePath + '/' + nodeName).replace('//', '/');
			NC.Progress.show('Creating nodes...', 1000);

			$.post(NC.REST.nodeLet + nodeFullPath, {body: ''},function (response) {

				NC.Progress.hide();

			}).fail(function (e) {
				NC.Progress.fail("Updating node property has failed : " + '[' + e.status + ']' + e.statusText, 1000);
			});
		};

		$('#nodeNameEditorPanel .action_button').on('click', function() {
			var nodeName = $('#node_name').val();

			saveNode(nodeName);

			if(_self.callback !== null) {
				_self.callback.apply(_self, [nodeName]);
			}

			nodeNameEditorPanel.modal('hide');
		});

		$('#nodeNameEditorPanel .btn-default').on('click', function() {
			Stage.detachConnection(_self.currentConnection);
		});

		this.currentConnection = null;

		this.show = function(originalId, parentNodePath, connection) {
			_self.originalId = originalId;
			_self.parentNodePath = parentNodePath;
			_self.currentConnection = connection;

			nodeNameEditorPanel.modal('show');
		};

		this.callback = function(func) {
			_self.callback = func;
			return _self;
		}

	}

	window.NodeNameDialog = NodeNameDialog;

})(jQuery, window);
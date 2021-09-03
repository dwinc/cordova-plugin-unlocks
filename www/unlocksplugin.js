var exec = require('cordova/exec');

function execPromise(action, args) {
	return new Promise((resolve, reject) => {
		exec(resolve, reject, 'UnlocksPlugin', action, args);
	});
}

module.exports = {
	unlockEvents: function(begin, end) {
		return execPromise('unlockEvents', [begin, end]);
	},
	unlockStats: function(begin, end) {
		return execPromise('unlockStats', [begin, end]);
	},
	hasPermission: function() {
		return execPromise('hasPermission', []);
	},
	requestPermission: function() {
		return execPromise('requestPermission', []);
	},
};

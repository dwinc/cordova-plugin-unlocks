// Empty constructor
function UnlocksPlugin() {}

UnlocksPlugin.prototype.unlocks = function(start, end, successCallback, errorCallback) {
  var options = {};
  options.start = start;
  options.end = end;
  cordova.exec(successCallback, errorCallback, 'UnlocksPlugin', 'unlocks', [options]);
}

// Installation constructor that binds UnlocksPlugin to window
UnlocksPlugin.install = function() {
  if (!window.plugins) {
    window.plugins = {};
  }
  window.plugins.unlocksPlugin = new UnlocksPlugin();
  return window.plugins.unlocksPlugin;
};
cordova.addConstructor(UnlocksPlugin.install);
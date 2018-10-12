// Empty constructor
function GeolocationPlugin() {}

// The function that passes work along to native shells
// Message is a string, duration may be 'long' or 'short'
GeolocationPlugin.prototype.show = function(message, duration, successCallback, errorCallback) {
  var options = {};
  options.message = message;
  options.duration = duration;
  cordova.exec(successCallback, errorCallback, 'GeolocationPlugin', 'show', [options]);
}

// Installation constructor that binds ToastyPlugin to window
GeolocationPlugin.install = function() {
  if (!window.plugins) {
    window.plugins = {};
  }
  window.plugins.geolocationPlugin = new GeolocationPlugin();
  return window.plugins.geolocationPlugin;
};
cordova.addConstructor(GeolocationPlugin.install);
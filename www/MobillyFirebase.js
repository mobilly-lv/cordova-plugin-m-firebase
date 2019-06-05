let exec = require('cordova/exec');

let MobillyFirebase = function() {
};

MobillyFirebase.initialize = function(success, error) {
    exec(success, error, 'MobillyFirebase', 'initialize');
};

MobillyFirebase.onNotification = function(success, error) {
    exec(success, error, 'MobillyFirebase', 'onNotification');
};

MobillyFirebase.setUserId = function(userId,success, error) {
    exec(success, error, 'MobillyFirebase', 'setUserId',[userId]);
};

MobillyFirebase.logEvent = function (name, params, success, error) {
    exec(success, error, "MobillyFirebase", "logEvent", [name, params]);
};

MobillyFirebase.logError = function (message, success, error) {
    exec(success, error, "MobillyFirebase", "logError", [message]);
};

module.exports = MobillyFirebase;

/*exports.echojs = function(arg0, success, error) {
    if (arg0 && typeof(arg0) === 'string' && arg0.length > 0) {
        success(arg0);
    } else {
        error('Empty message!');
    }
};*/

/**
 * Utilities and shared functionality for the build hooks.
 */
let fs = require('fs');
let path = require("path");

fs.ensureDirSync = function (dir) {
    if (!fs.existsSync(dir)) {
        dir.split(path.sep).reduce(function (currentPath, folder) {
            currentPath += folder + path.sep;
            if (!fs.existsSync(currentPath)) {
                fs.mkdirSync(currentPath);
            }
            return currentPath;
        }, '');
    }
};

module.exports = {
    /**
     * Used to get the name of the application as defined in the config.xml.
     *
     * @param {object} context - The Cordova context.
     * @returns {string} The value of the name element in config.xml.
     */
    getAppName: function (context) {
        let ConfigParser = context.requireCordovaModule("cordova-lib").configparser;
        let config = new ConfigParser("config.xml");
        return config.name();
    },


    copyKey: function (platform) {
        for (let i = 0; i < platform.src.length; i++) {
            let file = platform.src[i];
            if (this.fileExists(file)) {
                try {
                    let contents = fs.readFileSync(file).toString();

                    try {
                        platform.dest.forEach(function (destinationPath) {
                            let folder = destinationPath.substring(0, destinationPath.lastIndexOf('/'));
                            fs.ensureDirSync(folder);
                            fs.writeFileSync(destinationPath, contents);
                        });
                    } catch (e) {
                        // skip
                    }
                } catch (err) {
                    console.log(err);
                }

                break;
            }
        }
    },

    getValue: function (config, name) {
        let value = config.match(new RegExp('<' + name + '(.*?)>(.*?)</' + name + '>', 'i'));
        if (value && value[2]) {
            return value[2]
        } else {
            return null
        }
    },

    fileExists: function (path) {
        try {
            return fs.statSync(path).isFile();
        } catch (e) {
            return false;
        }
    },

    directoryExists: function (path) {
        try {
            return fs.statSync(path).isDirectory();
        } catch (e) {
            return false;
        }
    }
};

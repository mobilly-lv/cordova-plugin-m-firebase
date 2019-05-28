let fs = require('fs');
let path = require('path');
let utilities = require("./lib/utilities");
let configParser = require('./lib/configXmlParser.js');
// let config = fs.readFileSync('config.xml').toString();
// let name = utilities.getValue(config, 'name');



module.exports = function(context) {

    let name = utilities.getAppName(context);
    let IOS_DIR = 'platforms/ios';
    let ANDROID_DIR = 'platforms/android';
    let IOS_RESOURCE_DIR = `${IOS_DIR}/${name}/Resources`;
    let ANDROID_RESOURCE_DIR = `${ANDROID_DIR}/app/src/main/res`;

    let haveAndroidPlatform = fs.existsSync("platforms/android");
    let haveIosPlatform = fs.existsSync("platforms/ios");

    let config = configParser.readPreferences(context);

    let colorXmlSrc=config.colorsXmlSrc;
    let stringsXmlSrc=config.stringsXmlSrc;
    let googleServicesPlistSrc =config.googleServicesPlistSrc;
    let googleServicesJsonSrc=config.googleServicesJsonSrc;

    if(haveIosPlatform){
        if(fs.existsSync(googleServicesPlistSrc)){
            console.info(`m-firebase: Copying ${googleServicesPlistSrc}`);
            fs.createReadStream(googleServicesPlistSrc).pipe(
                fs.createWriteStream(`${IOS_RESOURCE_DIR}/GoogleService-Info.plist`));
        }
    }
    if(haveAndroidPlatform){
        if(fs.existsSync(colorXmlSrc)){
            console.info(`m-firebase: Copying ${colorXmlSrc}`);
            fs.createReadStream(colorXmlSrc).pipe(
                fs.createWriteStream(`${ANDROID_RESOURCE_DIR}/values/m-firebase-color.xml`));
        }
        if(fs.existsSync(stringsXmlSrc)){
            console.info(`m-firebase: Copying ${stringsXmlSrc}`);
            fs.createReadStream(stringsXmlSrc).pipe(
                fs.createWriteStream(`${ANDROID_RESOURCE_DIR}/values/m-firebase-strings.xml`));
        }

        if(fs.existsSync(googleServicesJsonSrc)){
            console.info(`m-firebase: Copying ${googleServicesJsonSrc}`);
            fs.createReadStream(googleServicesJsonSrc).pipe(
                fs.createWriteStream(`${ANDROID_DIR}/app/google-services.json`));
        }

        for (const icon of config["ic-notification"]){
            let _destFile = `${ANDROID_RESOURCE_DIR}/drawable-${icon.size}/ic_notification.png`;
            let destDir = path.dirname(_destFile);
            console.info(`m-firebase: Copying ${icon.src}`);
            if (fs.existsSync(icon.src) && fs.existsSync(destDir))
                fs.createReadStream(icon.src).pipe(fs.createWriteStream(_destFile));
        }
    }



};



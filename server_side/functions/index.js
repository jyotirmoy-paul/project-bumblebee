const functions = require('firebase-functions');
const admin = require('firebase-admin');
admin.initializeApp(functions.config().firebase);

// function to convert from degree to radian
function deg2rad(deg) {
    return deg * (Math.PI/180)
}

// function to get the straight line distance between two lat long
function getDistanceFromLatLonInKm(lat1,lon1,lat2,lon2) {

    var R = 6371; // Radius of the earth in km
    var dLat = deg2rad(lat2-lat1);  // deg2rad below
    var dLon = deg2rad(lon2-lon1); 
    var a = 
    Math.sin(dLat/2) * Math.sin(dLat/2) +
    Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * 
    Math.sin(dLon/2) * Math.sin(dLon/2);

    var c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a)); 
    var d = R * c; // Distance in km
    return d;
}

/*
The following function finds the nearest delivery agent to a donor location, then informs all the admins about
the donation availability and the nearest delivery agent, along with the main photo uploaded by the donor
*/
exports.findNearestAgent = functions.database.ref('/donation_details/{id}/donation_data').onCreate((snap, context) => {

    const donationData = snap.val();

    // extract donor details thats shown to the admin
    const donationKey = donationData.donationKey;
    const donationCategory = donationData.donationCategory;
    const donorName = donationData.donorName;
    const donorPhNumber = donationData.donorContactNumber;
    const donationMainPhoto = donationData.donationMainPhotoUrl;
    const donorToken = donationData.userRegTokenKey;
    const donorUID = donationData.donorUID;

    // extract the position of the donor
    const DonorLat = donationData.latLong.latitude;
    const DonorLong = donationData.latLong.longitude;

    return admin.database().ref('/delivery_agent_data').once('value', (snap) => {

        var shortestAgentData = undefined;
        var minDistance = Infinity;

        // looping through every delivery agent data
        snap.forEach(snapShot => {
            var DelAgentLat = snapShot.val().contact_info.latLong.latitude;
            var DelAgentLong = snapShot.val().contact_info.latLong.longitude;

            var isVerified = snapShot.val().user_info.isVerified;
            var isAvailable = snapShot.val().contact_info.isAvailable;

            var distance = getDistanceFromLatLonInKm(DonorLat, DonorLong, DelAgentLat, DelAgentLong);

            if(distance < minDistance && isAvailable === "yes" && isVerified === "yes"){
                minDistance = distance;
                shortestAgentData = snapShot; // store the contact info of the shortest delivery agent
            }
        });

        // if no agents are available
        if(typeof shortestAgentData === 'undefined'){

            // then delete the posted donation details and send notification to the user telling him/her to try later

            // sending notification to user
            noAgentAvailableNotification = {
                notification: {
                    title: 'Ooops! No one\'s available',
                    body: 'Sorry, ' + donorName + '! No delivery agent is available.'
                },
                token: donorToken
            }

            admin.messaging().send(noAgentAvailableNotification);

            // remove the main photo from firebase-storage
            admin.storage().bucket().file(donationKey).delete();

            // delete the database entry
            return admin.database().ref('/donation_details/' + donationKey + '/').remove();
        }

        // if a delivery agent is available

        const deliveryAgent = shortestAgentData.val();

        // delivery agent data shown to admin
        const deliveryAgentName = deliveryAgent.user_info.name;
        const deliveryAgentNumber = deliveryAgent.user_info.phoneNumber;
        const deliveryAgentUID = deliveryAgent.contact_info.UID;
        const agentToken = deliveryAgent.contact_info.firebaseToken;

        // now send a notification to the admins informing about a new donation availability
        return admin.database().ref('/admin_data').once('value', (sn) =>{

            var admins = []
            sn.forEach(snapSht => {
                admins.push(snapSht.val().user_info);
            });

            var notifications = [];
            var i = 0;

            // send notification to all the available admins
            for(i = 0; i < admins.length; i++){
                var notificationMessage = {
                    notification: {
                        title: "New donation available",
                        body: "Hey, " + admins[i].name + ", A new available donation needs your attention!"
                    },
                    token: admins[i].firebaseToken
                }
                notifications.push(notificationMessage);
            }

            // finally send messages to the admins
            for(i = 0; i < notifications.length; i++){
                admin.messaging().send(notifications[i]);
            }

            // finally let the admin know the following details
            var donationData = {
                donationKey: donationKey,
                deliveryAgentUID: deliveryAgentUID,
                distanceInKm: minDistance,
                donationConfirm: "-",
                donorName: donorName,
                donorContactNumber: donorPhNumber,
                donationCategory: donationCategory,
                deliveryAgentName: deliveryAgentName,
                deliveryAgentNumber: deliveryAgentNumber,
                agentToken: agentToken,
                donationMainPhoto: donationMainPhoto,
                donationStatus: "-",
                donorToken: donorToken,
                donorUID: donorUID
            };

            return admin.database().ref('/donation_data_under_process/').child(donationKey).set(donationData);

        });

    });

});


/*
The following function determines the admin decision about approving or rejecting an donation request and 
notifying the user or delivery agent accordingly
*/
exports.notifyDeliveryAgent = functions.database.ref('/donation_data_under_process/{id}').onUpdate((snap, context) => {

    const data = snap.after.val(); // get the changed value

    // donation not yet delivered
    if(data.donationStatus === "collected"){
        // this is no important event to trigger anything
        return null;
    }

    // else if donation has not yet been collected and the donation has been confirmed by the admin
    if(data.donationConfirm === "no"){

        // then notify the donor about this cancellation
        let notification = {
            notification: {
                title: "Donation Request Cancelled",
                body: "Hello, " + data.donorName + "! Your donation request has been rejected due to its vagueness."
            },
            token: data.donorToken
        }

        admin.messaging().send(notification);

        // delete the storage and database data using the admin app only
        return null;

    }

    // now if the donation request has been accepted ---> let the lucky delivery agent know it
    let agentNotification = {
        notification: {
            title: "New Donation Available",
            body: "Hey, " + data.deliveryAgentName + "! A donation pickup has been scheduled, please check your app."
        },
        token: data.agentToken
    }

    let donorNotification = {
        notification: {
            title: "Donation Request Accepted",
            body: "Hurray! Your donation request has been scheduled to be picked up by " + data.deliveryAgentName
        },
        token: data.donorToken
    }

    // send notifications to delivery agent and donor
    admin.messaging().send(donorNotification);
    admin.messaging().send(agentNotification);

    var key = data.donationKey;
    return admin.database().ref('/donation_details/' + key + "/donation_data/").once('value', (donationDataSnap) => {

        const donationDataSnapShot = donationDataSnap.val();

        // finally push those donationData into the agent 
        return admin.database().ref('/approved_donation_requests/').child(data.deliveryAgentUID).child(key).set(donationDataSnapShot);

    });

});

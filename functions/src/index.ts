import * as admin from 'firebase-admin';
import * as functions from 'firebase-functions';
import * as nodemailer from 'nodemailer';

admin.initializeApp();

const transporter = nodemailer.createTransport({
    service: 'gmail',
    auth: {
        user: functions.config().email.user,
        pass: functions.config().email.password
    }
});

export const sendTeamInvitation = functions.https.onCall(async (data, context) => {
    if (!context.auth) {
        throw new functions.https.HttpsError('unauthenticated', 'User must be authenticated');
    }

    const { teamName, eventId, inviterEmail, inviteeEmail } = data;

    // Get event details
    const eventDoc = await admin.firestore().collection('events').doc(eventId).get();
    const event = eventDoc.data();

    if (!event) {
        throw new functions.https.HttpsError('not-found', 'Event not found');
    }

    const mailOptions = {
        from: functions.config().email.user,
        to: inviteeEmail,
        subject: `Team Invitation: ${teamName} - ${event.title}`,
        html: `
            <h2>You've been invited to join a team!</h2>
            <p><strong>${inviterEmail}</strong> has invited you to join their team <strong>${teamName}</strong> for the event <strong>${event.title}</strong>.</p>
            <h3>Event Details:</h3>
            <ul>
                <li>Date: ${new Date(event.startDate).toLocaleDateString()}</li>
                <li>Location: ${event.location}</li>
                <li>Type: ${event.eventType}</li>
            </ul>
            <p>Please log in to the MyHackX app to accept or decline this invitation.</p>
            <p>Good luck!</p>
        `
    };

    await transporter.sendMail(mailOptions);
});

export const sendTeamNotification = functions.https.onCall(async (data, context) => {
    if (!context.auth) {
        throw new functions.https.HttpsError('unauthenticated', 'User must be authenticated');
    }

    const { type, teamName, eventId, memberEmail, leaderEmail } = data;

    // Get event details
    const eventDoc = await admin.firestore().collection('events').doc(eventId).get();
    const event = eventDoc.data();

    if (!event) {
        throw new functions.https.HttpsError('not-found', 'Event not found');
    }

    let mailOptions;
    switch (type) {
        case 'MEMBER_JOINED':
            mailOptions = {
                from: functions.config().email.user,
                to: leaderEmail,
                subject: `New Team Member - ${teamName}`,
                html: `
                    <h2>New Team Member Joined</h2>
                    <p><strong>${memberEmail}</strong> has joined your team <strong>${teamName}</strong> for the event <strong>${event.title}</strong>.</p>
                    <p>Log in to the MyHackX app to view your team details.</p>
                `
            };
            break;

        case 'INVITATION_DECLINED':
            mailOptions = {
                from: functions.config().email.user,
                to: leaderEmail,
                subject: `Team Invitation Declined - ${teamName}`,
                html: `
                    <h2>Team Invitation Declined</h2>
                    <p><strong>${memberEmail}</strong> has declined to join your team <strong>${teamName}</strong> for the event <strong>${event.title}</strong>.</p>
                    <p>You can invite other members through the MyHackX app.</p>
                `
            };
            break;

        case 'MEMBER_REMOVED':
            mailOptions = {
                from: functions.config().email.user,
                to: memberEmail,
                subject: `Team Membership Update - ${teamName}`,
                html: `
                    <h2>Team Membership Update</h2>
                    <p>You have been removed from the team <strong>${teamName}</strong> for the event <strong>${event.title}</strong> by the team leader.</p>
                    <p>You can join or create another team through the MyHackX app.</p>
                `
            };
            break;

        default:
            throw new functions.https.HttpsError('invalid-argument', 'Invalid notification type');
    }

    await transporter.sendMail(mailOptions);
}); 
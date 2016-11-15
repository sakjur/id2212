package is.mjuk.sockets.meetup;

/**
 * MeetupCallbackInterface for observers that needs to know when the MeetupRunner
 * is ready and done
 */
public interface MeetupCallbackInterface {
    public void MeetupCallback(MeetupRunner.CallbackType cb);
}

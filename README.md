# jSkype
jSkype creation started when skype4web was released, however at the time I was making a private Skype client in Java, not an API. Samczsun, better known as super salter 9000 was creating an extremely limited api at the time of my client creation and still is today. In order to spare people from his limited api, I'm releasing jSkype. 

#Features (confirmed)
- Ping chats with images
- Get contact requests
- Get recent groups
- Get contacts
- Add and remove users from groups
- Send messages
- Format messages
- Accept contact requests
- Send contact requests
- User information
- Search Skype's DB
- Change your profile picture
- Get info about yourself
- Always online (Doesn't break after 2 days plus can survive internet shortage)
 
#Events
- Edit message (UserChatEvent#isEdited)
- TopicChangedEvent
- UserChatEvent
- UserImagePingEvent
- UserOtherFilesPingEvent
- UserJoinEvent
- UserLeaveEvent
- UserPendingContactRequestEvent
- UserTypingEvent

#Downloads, Javadocs, etc
JavaDocs: http://gghost.xyz/JavaDocs/jSkype

Maven: http://maven.gghost.xyz OR http://ghosted.me/maven

Repository:
```
 <repository>
  <id>xyz.gghost</id>
  <url>http://gghost.xyz/maven/</url>
</repository>
```
Dependency:
```
<dependency>
  <groupId>xyz.gghost</groupId>
  <artifactId>jskype</artifactId>
  <version>2.18-BETA</version>
  <scope>compile</scope>
</dependency>
```

#Creating a skype instance
Before creating a Skype instance, you'll need to confirm whether or not you login with an email/pass or user/pass. If you login with a username and password, you can create a new instance of SkypeAPI with the arguments (username, password), otherwise people with email logins should pass (email, username, password)

Example user/pass: 
```java
SkypeAPI skype = new SkypeAPI("NotGhostBot", "Password");
```
Example email/pass: 
```java
SkypeAPI skype = new SkypeAPI("ghost@ghosted.me", "NotGhostBot", "Password");
```
If getting groups is taking a while, you can add another boolean to enable experimental multithreading. It isn't recommended but is useful in bot environments
#Where are all the methods?
jSkype is split up into two main classes; Skype and SkypeAPI. SkypeAPI is mainly useless, however it contains the Skype instance, which is where the recent groups, contacts, send messages, etc is hold. Checking the JavaDocs would help out, but it's safe to assume most of what you'll want is in SkypeAPI#getSkype (Skype)

API Related (event listeners, command handlers, Skype instance, etc): SkypeAPI

User related (contact requests, active groups, contacts, login, etc): Skype
#Sending chat messages
Sending a message to all contacts example:
```java
for (User user : skype.getSkype().getContacts()){
  user.sendMessage(skype, "Hi");
}
```
Sending a message to all recent groups and contacts example:
```java
for (Conversation group : skype.getSkype().getRecent()){
  group.sendMessage(skype, "Hi");
}
```
Editing a message:
```java
Message message = group.sendMessage(skype, "Hi");
message.editMessage("");
```
## Formatting messages
The "Chat" class is a utilities class for formatting. To format "hi" in bold, you can do "Chat.bold("hi")", which will return "hi" with the html Skype tags. If you wanted "hi" to be in bold and blink, you can do "Chat.bold(Chat.blink("hi"))". When sending raw messages, I highly suggest you encode them using Chat#encodeRawText

#Example event handler usage:
In order to listen for an event, create a class that implements EventListener, and register it by calling "api.getEventManager().registerListener(new YourListener(skype));" All event's can be found the "xyz.gghost.jskype.api" package and in the event section of this readme file. 

```java
public class ExampleListener implements EventListener {
    SkypeAPI api;
    public ExampleListener(SkypeAPI api){
        this.api = api;
    }
    public void join(UserJoinEvent e){ //If a method has the event as the only argument, it will get invoked once the events trigger(join/chat/leave/etc) has been called. 
        System.out.println(e.getUser().getDisplayName() + " has joined " + e.getGroup().getChatId());
    }
}

public class Test {
    static boolean isRunning = true;
    public static void main(String[] args) {
        SkypeAPI skype = new SkypeAPI("NotGhostBot", "{password here}"); //login
        System.out.println("Loaded Skype..."); //Tell the user that skype has fully initialized - getting contacts, recent, etc can take a few seconds

        skype.getEventManager().registerListener(new ExampleListener(skype)); //Register listener

        while (isRunning){} //This program is multithreaded and the main thread doesn't get used, so you'll want an (infinite) delay to keep the program open.
        skype.stop(); //Close Skype related threads - shutting it down

    }
}
```

#Example command handler usage:
###Feature removed!
###You can use the UserChatEvent instead 

#TODO
- Start using interfaces 
- Handle calls (Windows only + semi compatible with wine)
- Handle voice mail
- User promoted event

#Dependencies
- commons-lang 3
- org.json (repo contains fork)
- jsoup (repo contains sams authenticator)
- lombok


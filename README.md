# jSkype

Needs a recode as soon as possible although I'm trying to make what is here stable

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
- Change a groups topic
- User information
- Search Skype's DB
- Change your profile picture
- Set your online status
- Get info about yourself
- Always online (Doesn't break after 2 days plus can survive internet shortage)
- Create and join groups
- Promote and demote users
 
#Events
- Edit message (UserChatEvent#isEdited)
- TopicChangedEvent
- UserChatEvent
- UserImagePingEvent
- UserOtherFilesPingEvent
- UserRecaptchaEvent (recoded versions only!)
- UserJoinEvent
- UserLeaveEvent
- UserPendingContactRequestEvent
- UserTypingEvent
- ChatPictureChangedEvent
- UserNewMovieAdsPingEvent
- UserRoleChangedEvent
- UserStatusChangedEvent (online status)
- APILoadedEvent

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
  <version>4.1-BETA</version>
  <scope>compile</scope>
</dependency>
```

#Creating a skype instance
Before creating a Skype instance, you'll need to confirm whether or not you login with an email/pass or user/pass. If you login with a username and password, you can create a new instance of SkypeAPI with the arguments (username, password).

Example user/pass: 
```java
SkypeAPI skype = new SkypeAPI("NotGhostBot", "Password");
skype.login();
```

#Sending chat messages
Sending a message to all contacts example:
```java
for (User user : skype.getContacts()){
  user.getGroup().sendMessage("Hi");
}
```
Sending a message to all recent groups and contacts example:
```java
for (Group group : skype.getGroups()){
  group.sendMessage("Hi");
}
```
Editing a message:
```java
Message message = group.sendMessage(skype, "Hi");
message.editMessage("");
```
## Formatting messages

MessageBuilder is the builder class for constructing string that is safe to pass to Group#sendMessage. In order to add text to the message builder, use #addText. Only use #addHtml with past outputs from #build and html code you know is safe. If you'd like to add two message builders together, simply builderA.build() + builderB.build() would work, however I recommend you to pass the old build output to the constructor of the new builder instance, if you want to make a clean message builder. Otherwise, you can use the FormatUtils class for small, quick jobs.

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
        skype.login();
        System.out.println("Loaded Skype..."); //Tell the user that skype has fully initialized - getting contacts, recent, etc can take a few seconds

        skype.getEventManager().registerListener(new ExampleListener(skype)); //Register listener

        while (isRunning){} //This program is multithreaded and the main thread doesn't get used, so you'll want an (infinite) delay to keep the program open.
        skype.stop(); //Close Skype related threads - shutting it down

    }
}
```

#TODO
- Handle calls (Windows only + semi compatible with wine)

#Dependencies
- commons-lang 3
- org.json (repo contains fork)
- jsoup 
- lombok

#LICENSE
See LICENSE file 

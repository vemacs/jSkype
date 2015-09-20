package xyz.gghost.jskype.user;

/**
 * Created by Ghost on 19/09/2015.
 */
public class GroupUser{
    public final Role role;
    private User user;
    public GroupUser(User user, Role role){
        this.user = user;
        this.role = role;
    }
    public User getUser(){
        return user;
    }
    public enum Role{
        MASTER, USER
    }

}

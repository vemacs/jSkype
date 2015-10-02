package xyz.gghost.jskype.user;

import xyz.gghost.jskype.internal.impl.GroupImpl;

/**
 * Created by Ghost on 19/09/2015.
 */
public class GroupUser{
    public final Role role;
    private User user;
    private GroupImpl group;
    public GroupUser(User user, Role role, GroupImpl group){
        this.user = user;
        this.role = role;
        this.group = group;
    }
    @Override
    public String toString(){
        return user.getUsername();
    }
    public void setIsAdmin(boolean admin){
        group.setAdmin(user.getUsername(), admin);
    }
    public User getUser(){
        return user;
    }
    public enum Role{
        MASTER, USER
    }
}

package xyz.gghost.jskype.user;

import lombok.AllArgsConstructor;
import xyz.gghost.jskype.internal.impl.GroupImpl;

@AllArgsConstructor
public class GroupUser{
    private User user;
    public Role role;
    private GroupImpl group;

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

public class AppUser
{
    private String username;
    private String role;


    public AppUser(String username, String role)
    {
        this.username = username;
        this.role = role;
    }

    public String getUsername()
    {
        return username;
    }
    public String getRole()
    {
        return role;
    }

    public void setRole(String role)
    {
        this.role = role;
    }
}
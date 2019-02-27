package ru.tn.managedBean;

import ru.tn.sessionBean.HeaderTemplateSBean;

import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import java.io.Serializable;
import java.security.Principal;

@ManagedBean(name = "headerTemplate")
@SessionScoped
public class HeaderTemplateMBean implements Serializable {

    @EJB
    private HeaderTemplateSBean bean;

    private String user;
    private String userPrincipal;

    public String logout() {
        FacesContext.getCurrentInstance().getExternalContext().invalidateSession();
        return "/view/authentication/login.xhtml?faces-redirect=true";
    }

    public boolean isUserAllowedAccess() {
        return FacesContext.getCurrentInstance().getExternalContext().isUserInRole("ADMIN");
    }

    public String getUser() {
        if (user == null) {
            Principal principal = FacesContext.getCurrentInstance().getExternalContext().getUserPrincipal();
            if (principal != null) {
                userPrincipal = principal.getName();
                user = bean.getUserDescription(principal.getName());
            }
        }
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getUserPrincipal() {
        return userPrincipal;
    }

    public void setUserPrincipal(String userPrincipal) {
        this.userPrincipal = userPrincipal;
    }
}

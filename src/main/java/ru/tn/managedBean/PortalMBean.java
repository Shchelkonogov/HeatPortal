package ru.tn.managedBean;

import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import javax.faces.context.FacesContext;
import java.io.Serializable;

@ManagedBean(name = "portal")
@ApplicationScoped
public class PortalMBean implements Serializable {

    public void keepSessionAlive() {
    }

    public void logout() {
        System.out.println(FacesContext.getCurrentInstance().getExternalContext().getUserPrincipal().getName());
        FacesContext.getCurrentInstance().getExternalContext().invalidateSession();
    }
}

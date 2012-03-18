/*
 * Copyright 2010 Wyona
 */
package org.wyona.yanel.impl.resources.yaneluser;

import org.wyona.yanel.core.ResourceConfiguration;
import org.wyona.yanel.impl.resources.BasicXMLResource;

import org.wyona.security.core.api.User;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import org.wyona.commons.xml.XMLHelper;

/**
 * A resource to edit/update the profile of a user
 */
public class EditYanelUserProfileResource extends BasicXMLResource {
    
    private static Logger log = Logger.getLogger(EditYanelUserProfileResource.class);

    private String transformerParameterName;
    private String transformerParameterValue;

    private static final String USER_PROP_NAME = "user";
    
    /*
     * @see org.wyona.yanel.impl.resources.BasicXMLResource#getContentXML(String)
     */
    protected InputStream getContentXML(String viewId) {
        if (log.isDebugEnabled()) {
            log.debug("requested viewId: " + viewId);
        }

        String oldPassword = getEnvironment().getRequest().getParameter("oldPassword");
        if (oldPassword != null) {
            updatePassword(oldPassword);
        }

        String email = getEnvironment().getRequest().getParameter("email");
        if (email != null) {
            updateProfile(email);
        }

        try {
            return getXMLAsStream();
        } catch(Exception e) {
            log.error(e, e);
            return null;
        }
    }

    /**
     * Get user profile as XML as stream
     */
    private java.io.InputStream getXMLAsStream() throws Exception {
        String userId = getUserId();
        if (userId != null) {
            User user = realm.getIdentityManager().getUserManager().getUser(userId);

            Document doc = XMLHelper.createDocument(null, "user");
            Element rootEl = doc.getDocumentElement();
            rootEl.setAttribute("id", userId);
            rootEl.setAttribute("email", user.getEmail());
            rootEl.setAttribute("lamguage", user.getLanguage());

            Element nameEl = doc.createElement("name");
            nameEl.setTextContent(user.getName());
            rootEl.appendChild(nameEl);

            Element expirationDateEl = doc.createElement("expiration-date");
            expirationDateEl.setTextContent("" + user.getExpirationDate());
            rootEl.appendChild(expirationDateEl);

            Element descEl = doc.createElement("description");
            descEl.setTextContent("" + user.getDescription());
            rootEl.appendChild(descEl);

            org.wyona.security.core.api.Group[] groups = user.getGroups();
            if (groups !=  null && groups.length > 0) {
                Element groupsEl = doc.createElement("groups");
                rootEl.appendChild(groupsEl);
                for (int i = 0; i < groups.length; i++) {
                    Element groupEl = doc.createElement("group");
                    groupEl.setAttribute("id", groups[i].getID());
                    groupsEl.appendChild(groupEl);
                }
            }

            return XMLHelper.getInputStream(doc, false, true, null);
        } else {
            return new java.io.StringBufferInputStream("<no-user-id/>");
        }
    }

    /**
     * Get user id from resource configuration
     */
    private String getUserId() throws Exception {
        String userId = null;

        // 1)
        userId = getEnvironment().getRequest().getParameter("id");
        if (userId != null) {
                return userId;
/*
            if (getRealm().getPolicyManager().authorize("/yanel/users/" + userId + ".html", getEnvironment().getIdentity(), new org.wyona.security.core.api.Usecase("view"))) { // INFO: Because the policymanager has no mean to check (or interpret) query strings we need to recheck programmatically
                return userId;
            } else {
                //throw new Exception("User '" + getEnvironment().getIdentity().getUsername() + "' tries to access user profile '" + userId + "', but is not authorized!");
                log.warn("User '" + getEnvironment().getIdentity().getUsername() + "' tries to access user profile '" + userId + "', but is not authorized!");
            }
*/
        } else {
            log.debug("User ID is not part of query string.");
        }

        // 2)
        userId = getResourceConfigProperty(USER_PROP_NAME);
        if (userId != null) {
            return userId;
        } else {
            log.debug("User ID is not configured inside resource configuration.");
        }

        // 3)
        userId = getPath().substring(getPath().lastIndexOf("/") + 1, getPath().lastIndexOf(".html"));
        if (userId != null && getRealm().getIdentityManager().getUserManager().existsUser(userId)) {
            return userId;
        } else {
            log.debug("Could not retrieve user ID from URL.");
        }

        // 4)
        userId = getEnvironment().getIdentity().getUsername();
        if (userId != null) {
            return userId;
        } else {
            log.warn("User does not seem to be signed in!");
        }

        throw new Exception("Cannot retrieve user ID!");
    }

    /**
     * Change user password
     *
     * @param oldPassword Existing current password
     */
    private void updatePassword(String oldPassword) {
        try {
            String userId = getUserId();

            if (!getRealm().getIdentityManager().getUserManager().getUser(userId).authenticate(oldPassword)) {
                setTransformerParameter("error", "Authentication of user '" +userId + "' failed!");
                log.error("Authentication of user '" + userId + "' failed!");
                return;
            }

            String newPassword = getEnvironment().getRequest().getParameter("newPassword");
            String newPasswordConfirmed = getEnvironment().getRequest().getParameter("newPasswordConfirmation");
            if (newPassword != null && !newPassword.equals("")) {
                if (newPassword.equals(newPasswordConfirmed)) {
                    User user = getRealm().getIdentityManager().getUserManager().getUser(userId);
                    user.setPassword(newPassword);
                    user.save();
                    setTransformerParameter("success", "Password updated successfully");
                } else {
                    setTransformerParameter("error", "New password and its confirmation do not match!");
                }
            } else {
                setTransformerParameter("error", "No new password was specified!");
            }
        } catch (Exception e) {
            log.error(e, e);
        }
    }

    /**
     * Update the email address (and possibly also the alias) inside user profile
     *
     * @param email New email address of user (and possibly also alias)
     */
    protected void updateProfile(String email) {
        if (email == null || ("").equals(email)) {
            setTransformerParameter("error", "emailNotSet");
        } else if (!validateEmail(email)) {
            setTransformerParameter("error", "emailNotValid");
        } else {
            try {
                String userId = getUserId();
                org.wyona.security.core.api.UserManager userManager = realm.getIdentityManager().getUserManager();
                User user = userManager.getUser(userId);
                String previousEmailAddress = user.getEmail();
                user.setEmail(email);
                //user.setName(getEnvironment().getRequest().getParameter("userName"));
                //user.setLanguage(getEnvironment().getRequest().getParameter("user-profile-language"));
                user.save();

                String[] aliases = user.getAliases();
                for (int i = 0; i < aliases.length; i++) {
                    if (aliases[i].equals(previousEmailAddress)) {
                        userManager.createAlias(email, userId);
                        userManager.removeAlias(previousEmailAddress);
                        log.warn("Alias updated, which means user needs to use new email '" + email + "' to login.");
                    }
                }

                setTransformerParameter("success", "E-Mail (and alias) updated successfully");
            } catch (Exception e) {
                log.error(e, e);
                setTransformerParameter("error", e.getMessage());
            }
        }
    }

    /**
     *
     */
    private void setTransformerParameter(String name, String value) {
        transformerParameterName = name;
        transformerParameterValue = value;
    }

    /**
     * @see org.wyona.yanel.impl.resources.BasicXMLResource#passTransformerParameters(Transformer)
     */
    protected void passTransformerParameters(javax.xml.transform.Transformer transformer) throws Exception {
        super.passTransformerParameters(transformer);
        try {
            if (transformerParameterName != null && transformerParameterValue != null) {
                transformer.setParameter(transformerParameterName, transformerParameterValue);
                transformerParameterName = null;
                transformerParameterValue = null;
            }
        } catch (Exception e) {
            log.error(e, e);
        }
    }

    /**
     * This method checks if the specified email is valid against a regex
     *
     * @param email
     * @return true if email is valid
     */
    private boolean validateEmail(String email) {
        String emailRegEx = "(\\w+)@(\\w+\\.)(\\w+)(\\.\\w+)*";
        Pattern pattern = Pattern.compile(emailRegEx);
        Matcher matcher = pattern.matcher(email);

        return matcher.find();
    }
}

package org.asciidoctor.jruby.ast.impl;

import org.asciidoctor.ast.Author;
import org.jruby.RubyStruct;
import org.jruby.javasupport.JavaEmbedUtils;

public class AuthorImpl implements Author {

    private static final String AUTHOR_KEY_NAME = "name";
    private static final String LASTNAME_KEY_NAME = "lastname";
    private static final String FIRST_NAME_KEY_NAME = "firstname";
    private static final String EMAIL_KEY_NAME = "email";
    private static final String INITIALS_KEY_NAME = "initials";
    private static final String MIDDLE_NAME_KEY_NAME = "middlename";

    private String fullName;
    private String lastName;
    private String firstName;
    private String middleName;
    private String email;
    private String initials;

    public static Author getInstance(RubyStruct authorStruct) {
        final AuthorImpl author = new AuthorImpl();
        author.setFullName(aref(authorStruct, AUTHOR_KEY_NAME));
        author.setFirstName(aref(authorStruct, FIRST_NAME_KEY_NAME));
        author.setMiddleName(aref(authorStruct, MIDDLE_NAME_KEY_NAME));
        author.setLastName(aref(authorStruct, LASTNAME_KEY_NAME));
        author.setInitials(aref(authorStruct, INITIALS_KEY_NAME));
        author.setEmail(aref(authorStruct, EMAIL_KEY_NAME));
        return author;
    }

    private static String aref(RubyStruct s, String key) {
        return (String) JavaEmbedUtils.rubyToJava(s.aref(s.getRuntime().newString(key)));
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getMiddleName() {
        return middleName;
    }

    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getInitials() {
        return initials;
    }

    public void setInitials(String initials) {
        this.initials = initials;
    }

    @Override
    public String toString() {

        StringBuilder authorStringRepresentation = new StringBuilder();

        if (this.getFullName() != null) {
            authorStringRepresentation.append(this.getFullName());
        }

        if (this.getEmail() != null) {
            authorStringRepresentation.append(" <").append(this.getEmail()).append(">");
        }

        return authorStringRepresentation.toString();
    }

}

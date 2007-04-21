/*
 * Copyright 2006 Wyona
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.wyona.org/licenses/APACHE-LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wyona.yanel.impl.navigation;

import org.wyona.yanel.core.navigation.Node;

import org.wyona.yarep.core.NoSuchNodeException;
import org.wyona.yarep.core.Path;
import org.wyona.yarep.core.Repository;

import org.apache.log4j.Category;

/**
 * RTI Implementation
 */
public class NodeResConfigAndDataRepoImpl implements Node {

    private static Category log = Category.getInstance(NodeResConfigAndDataRepoImpl.class);

    Path path;
    Repository repo; // resource configuration repository
    Repository dataRepo;

    /**
     *
     */
    public NodeResConfigAndDataRepoImpl(org.wyona.yarep.core.Repository resConfigRepo, org.wyona.yarep.core.Repository dataRepo, String path) {
        this.path = new Path(path);
        this.repo = resConfigRepo;
        this.dataRepo = dataRepo;
    }

    /**
     * @return new child
     */
    public Node insertAsFirstChild(Node child) {
        return null;
    }

    /**
     * @return new child
     */
    public Node insertBeforeChild(Node newChild, Node refChild) {
        return null;
    }

    /**
     * @return new child
     */
    public Node insertAfterChild(Node newChild, Node refChild) {
        return null;
    }

    /**
     * @return new child
     */
    public Node appendChild(Node child) {
        return null;
    }

    /**
     */
    public void removeChild(Node child) {
    }

    /**
     * Check if node is a resource
     */
    public boolean isResource() {
        if (isCollection()) return false;
        try {
            if (repo.getNode(path.toString() + ".yanel-rti").isResource()) return true;
        } catch (NoSuchNodeException e) {
            log.warn("No such node exception: " + path + ".yanel-rti");
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        try {
            if (repo.getNode(path.toString() + ".yanel-rc").isResource()) return true;
        } catch (NoSuchNodeException e) {
            log.warn("No such node exception: " + path + ".yanel-rc");
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return false;
    }

    /**
     * Check if node is a collection
     */
    public boolean isCollection() {
        try {
            log.debug("Check if node is a collection: " + path);
            if (repo.getNode(path.toString()).isCollection()) {
            //if (repo.isCollection(path)) {
                log.error("DEBUG: Is collection within repo: " + path);
                Path[] children = repo.getChildren(path);
                for (int i = 0; i < children.length; i++) {
                    if (children[i].getName().indexOf(".yanel-rti") > 0) {
                        return true;
                    }
                    if (children[i].getName().indexOf(".yanel-rc") > 0) {
                        return true;
                    }
                    if (repo.isCollection(children[i])) {
                        return true;
                    }
                }
            }
        } catch(NoSuchNodeException e) {
            log.warn("No such node exception: " + path);
            return false;
        } catch(Exception e) {
            log.error(e.getMessage(), e);
        }
        return false;
    }

    /**
     * Get children
     */
    public Node[] getChildren() {
        java.util.Vector c = new java.util.Vector();
        try {
            if (repo.isCollection(path)) {
                log.debug("Is collection within resource config repo: " + path);
                Path[] children = repo.getChildren(path);
                for (int i = 0; i < children.length; i++) {
                    if (repo.isCollection(children[i])) {
                        c.add(children[i].toString());
                    }
                    if (children[i].getName().indexOf(".yanel-rti") > 0) {
                        String cp = children[i].toString().substring(0, children[i].toString().indexOf(".yanel-rti"));
                        if (!repo.isCollection(new Path(cp))) c.add(cp);
                    }
                    if (children[i].getName().indexOf(".yanel-rc") > 0) {
                        String cp = children[i].toString().substring(0, children[i].toString().indexOf(".yanel-rc"));
                        if (!repo.isCollection(new Path(cp))) c.add(cp);
                    }
                }
            } else {
                log.debug("Is not a collection within resource config repo: " + path);
            }

            if (dataRepo.isCollection(path)) {
                log.debug("Is collection within data repository: " + path);
                Path[] children = dataRepo.getChildren(path);
                for (int i = 0; i < children.length; i++) {
                    log.error("DEBUG: " + children[i]);
/*
                    if (dataRepo.isCollection(children[i])) {
                        c.add(children[i].toString());
                    }
                    if (children[i].getName().indexOf(".yanel-rti") > 0) {
                        String cp = children[i].toString().substring(0, children[i].toString().indexOf(".yanel-rti"));
                        if (!dataRepo.isCollection(new Path(cp))) c.add(cp);
                    }
                    if (children[i].getName().indexOf(".yanel-rc") > 0) {
                        String cp = children[i].toString().substring(0, children[i].toString().indexOf(".yanel-rc"));
                        if (!dataRepo.isCollection(new Path(cp))) c.add(cp);
                    }
*/
                }
            } else {
                log.warn("Is not a collection within data repository: " + path);
            }
        } catch(Exception e) {
            log.error(e);
        }
        Node[] nodes = new Node[c.size()];
        for (int i = 0; i < c.size(); i++) {
            nodes[i] = new NodeResConfigAndDataRepoImpl(repo, dataRepo, (String) c.elementAt(i));
        }
        return nodes;
    }

    /**
     *
     */
    public Node getParent() {
        try {
	    return new NodeResConfigAndDataRepoImpl(repo, dataRepo, (String) repo.getNode(path.toString() + ".yanel-rc").getParent().getPath());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

    /**
     *
     */
    public Node getNextSibling() {
        return null;
    }

    /**
     *
     */
    public Node getPreviousSibling() {
        return null;
    }

    /**
     *
     */
    public String getPath() {
        return path.toString();
    }

    /**
     *
     */
    public String getName() {
        return path.getName();
    }
}

/*
 * Copyright 2000-2014 Vaadin Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.vaadin.sass.internal.tree;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.vaadin.sass.internal.ScssContext;
import com.vaadin.sass.internal.selector.Selector;
import com.vaadin.sass.internal.visitor.BlockNodeHandler;

public class BlockNode extends Node implements IVariableNode {

    private static final long serialVersionUID = 5742962631468325048L;

    private List<Selector> selectorList;

    public BlockNode(List<Selector> selectorList) {
        this.selectorList = selectorList;
    }

    public BlockNode(List<Selector> selectorList, Collection<Node> children) {
        this(selectorList);
        setChildren(children);
    }

    // for use by copy() only
    private BlockNode(BlockNode blockNode) {
        super(blockNode);
        selectorList = new ArrayList<Selector>(blockNode.selectorList);
    }

    public List<Selector> getSelectorList() {
        return Collections.unmodifiableList(selectorList);
    }

    public void setSelectorList(List<Selector> selectorList) {
        this.selectorList = selectorList;
    }

    public String buildString(boolean indent) {
        return buildString(indent, PRINT_STRATEGY);
    }

    @Override
    public String printState() {
        return buildString(false);
    }

    @Override
    public String toString() {
        return "BlockNode [" + buildString(true, TO_STRING_STRATEGY) + "]";
    }

    @Override
    public void replaceVariables(ScssContext context) {

        if (selectorList == null || selectorList.isEmpty()) {
            return;
        }

        ArrayList<Selector> newSelectorList = new ArrayList<Selector>();
        for (Selector s : selectorList) {
            newSelectorList.add(s.replaceVariables(context));
        }
        selectorList = newSelectorList;
    }

    public String getSelectors() {
        StringBuilder b = new StringBuilder();
        for (final Selector s : selectorList) {
            b.append(s);
        }

        return b.toString();
    }

    @Override
    public Collection<Node> traverse(ScssContext context) {
        ArrayList<Node> result = new ArrayList<Node>();
        try {
            replaceVariables(context);
            result.addAll(BlockNodeHandler.traverse(context, this));
        } catch (Exception e) {
            Logger.getLogger(BlockNode.class.getName()).log(Level.SEVERE, null,
                    e);
            // TODO is it correct to ignore the exception
        }
        return result;
    }

    private String buildString(boolean indent, BuildStringStrategy strategy) {
        StringBuilder string = new StringBuilder();
        int i = 0;
        for (final Selector s : selectorList) {
            string.append(s);
            if (i != selectorList.size() - 1) {
                string.append(", ");
            }
            i++;
        }
        string.append(" {\n");
        for (Node child : getChildren()) {
            if (indent) {
                string.append("\t");
            }
            string.append("\t" + strategy.build(child) + "\n");
        }
        if (indent) {
            string.append("\t");
        }
        string.append("}");
        return string.toString();
    }

    @Override
    public BlockNode copy() {
        return new BlockNode(this);
    }

}

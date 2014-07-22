package com.oldhawk.simbamenu;

import java.io.Serializable;

import org.w3c.dom.Node;

public class SerializableNode implements Serializable {  
	private static final long serialVersionUID = 1L;
	private Node itemNode;  
	public SerializableNode() {  
	}  
	public SerializableNode(Node n) {  
	    this.itemNode=n;  
	}  
	public Node getItemNode() {  
	    return this.itemNode;  
	}  
	public void setItemNode(Node n) {  
	    this.itemNode=n;  
	}  
}

package com.company;

public class MyLinkedList {
    public Node head;
    public String name;

    public MyLinkedList(String name) {
        this.name = name;
        this.head = null;
    }

    public MyLinkedList(String name, Double data) {
        this.name = name;
        this.head = new Node(data);
    }

    public void add(Double data) {
        if (head != null) {
            head.appendToTail(data);
        } else {
            head = new Node(data);
        }
    }

    public boolean contains(Double data) {
        Node current = head;

        while (current != null) {
            if (current.data.equals(data)) {
                return true;
            }
            else {
                current=current.next;
            }
        }
        return false;
    }

    public Double get(int data){
        Node current = head;
        for(int i=0;i<data;i++){
            if(current==null){
                return -1.0;
            }
            else{
                current=current.next;
            }
        }
        return current.data;
    }
    public void print() {
        Node current = head;
        System.out.print("HEAD-> ");
        while (current != null) {
            System.out.print(current.data + " -> ");
            current = current.next;
        }
        System.out.print("TAIL");
        System.out.println();
    }
    public void clear(){
        head.next=null;
        head=null;
    }
    public void delete(Double d) {
        Node newHead = new Node(0.);
        newHead.next = head;
        Node previous = newHead;
        Node current = head;

        while (current != null) {
            if (current.data.equals(d)) {
                previous.next = current.next;
            } else {
                previous = current;
            }
            current = current.next;
        }

        head = newHead.next;

    }
    public class Node {
        Node next = null;
        Node prev = null;
        Double data;

        public Node(Double data) {
            this.data = data;
        }

        void appendToTail(Double d) {

            Node end = new Node(d);
            Node n = head;
            while (n.next != null) {
                n = n.next;
            }
            n.next = end;
            end.prev = n;

        }
    }
}

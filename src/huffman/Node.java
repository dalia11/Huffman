
package huffman;

import java.util.Comparator;

/**
 *
 * @author RS
 */
public class Node {

    Node left, right;
    int value;
    char character;

    public Node(int value, char character) {
        this.value = value;
        this.character = character;
        left = null;
        right = null;
    }

    public Node(Node left, Node right) {
        this.value = left.value + right.value;
        if (left.value <= right.value) {
            this.right = right;
            this.left = left;
        } else {
            this.right = left;
            this.left = right;
        }
    }

    public Node() {
     }
 

}


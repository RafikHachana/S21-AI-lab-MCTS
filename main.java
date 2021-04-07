package com.company;


import java.util.*;

public class Main {

    public static void main(String[] args) {
	    System.out.println("Welcome to the MCTS Tic-Tac-Toe player");
        System.out.println("Be careful: The AI will get smarter the more rounds you play!");


        Tree t = new Tree();

        Scanner sc = new Scanner(System.in);

        t.root.board.printBoard();
        while(true)
        {
            Node curr = t.root;
            System.out.println();
            System.out.println();
            System.out.println();
            System.out.println("----- NEW GAME -----");
            System.out.println();
            System.out.println();


            for(int i=0;;i++)
            {
                //curr.visits++;

                if(curr.board.winner!=Board.IN_PROGRESS)
                {
                    MCTS.backPropagation(curr,curr.board.winner== Board.P2);
                    if(curr.board.winner== Board.P2)
                    {
                        System.out.println("YOU LOST :(");
                    }
                    else if(curr.board.winner==Board.P1)   System.out.println("YOU WON !!!");
                    else System.out.println("It's a draw -_- .");
                    break;
                }
                //P1: the user
                if(i%2==0)
                {
                    int[][] tmp = new int[3][3];
                    for(int j=0;j<3;j++) for(int k=0;k<3;k++) tmp[j][k]=curr.board.cell[j][k];
                    System.out.print("Enter the x,y coordinates of your next move :");
                    int x=sc.nextInt(),y=sc.nextInt();
                    tmp[x][y] = Board.P1;
                    if(curr.child.keySet().size()==0)
                    {
                        MCTS.expansion(curr,Board.P1);
                    }
                    Board tmp_b = new Board(tmp);
                    curr = curr.child.get(tmp_b);

                }
                //P2: The MCTS algorithm
                else {
                    System.out.println("AI's turn (using MCTS):");
                    if(curr.child.size()==0){
                        MCTS.simulation(curr);
                    }
                    curr = MCTS.selection(curr);
                }

                curr.board.printBoard();
            }
        }
    }
}
//stores a pair (x,y)
class Position {
    int x,y;
    public Position(int x,int y)
    {
        this.x = x;
        this.y = y;
    }
}


//node and tree classes to store the tree
class Node {
    HashMap<Board,Node> child;
    Node parent;
    Board board;
    int wins;
    int visits;
    public Node(Node p, Board b){
        child = new HashMap<>();
        parent= p;
        board = b;
        wins = 0;
        visits = 0;
    }
}

class Tree {
    Node root;
    public Tree()
    {
        int[][] cells = {{0,0,0},{0,0,0},{0,0,0}};
        Board b = new Board(cells);
        root = new Node(null,b);
    }
}

//the MCTS algorithm
class MCTS {

    //selection phase using the UTC formula
    static Node selection(Node curr)
    {
        double max = 0;
        Node res = null;
        for(Node i:curr.child.values()){
            double weight = (double)i.wins/i.visits + Math.sqrt(2)*Math.sqrt(Math.log(curr.visits)/(double)i.visits);
            if(weight>max)
            {
                max = weight;
                res = i;
            }
        }
        return res;
    }

    //expansion: adding children to a node
    static void expansion(Node curr,int player)
    {
        ArrayList<Position> empty = curr.board.emptyCells();
        for(Position p:empty)
        {
            int[][] tmp = new int[3][3];
            for(int j=0;j<3;j++) for(int k=0;k<3;k++) tmp[j][k]=curr.board.cell[j][k];
            tmp[p.x][p.y] = player;
            Board b = new Board(tmp);
            Node child = new Node(curr,b);
            curr.child.put(b,child);

        }
    }

    //starts from node, simulates random playout and registers results on the tree
    static void simulation(Node curr)
    {
        final int iterations = 30000;
        for(int i=0;i<iterations;i++)
        {
            Node tmp = curr;
            for(int turn=0;tmp.board.winner==-1;turn++)
            {
                Random rand = new Random();
                //tmp.visits++;
                if(tmp.child.size()==0){
                    MCTS.expansion(tmp,(turn%2==0?Board.P2:Board.P1));
                }
                int next = rand.nextInt(tmp.child.keySet().size());
                int counter = 0;
                //Node res =
                for(Board b:tmp.child.keySet()) {
                    if (counter == next) {
                        tmp = tmp.child.get(b);
                        break;
                    }
                    counter++;
                }
            }
            backPropagation(tmp,tmp.board.winner==Board.P2);

        }
    }

    //propagates new results on the path up to the root
    static void backPropagation(Node curr,boolean win)
    {
        if(curr==null) return;
        if(win) curr.wins++;
        curr.visits++;
        backPropagation(curr.parent,win);
    }
}

//represents the state of the board
class Board {
    int[][] cell;
    int winner;
    public static final int DEFAULT_BOARD_SIZE = 3;
    public static final int IN_PROGRESS = -1;
    public static final int DRAW = 0;
    public static final int P1 = 1;
    public static final int P2 = 2;


    public Board(int[][] curr){
        cell = curr;
        winner = findWinner();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Board board = (Board) o;
        for(int i=0;i<3;i++) for(int j=0;j<3;j++) if(cell[i][j]!=board.cell[i][j]) return false;
        return true;
    }

    @Override
    public int hashCode() {
        String to_hash = "";
        for(int i=0;i<3;i++) for(int j=0;j<3;j++) to_hash.concat(String.valueOf(cell[i][j]));
        return to_hash.hashCode();
    }

    public int findWinner()
    {
        //horizontal
        if(cell[1][1]==cell[1][2] && cell[1][1]==cell[1][0] && cell[1][0]!=0) return cell[1][1];
        if(cell[0][1]==cell[0][2] && cell[0][1]==cell[0][0] && cell[0][0]!=0) return cell[0][0];
        if(cell[2][1]==cell[2][2] && cell[2][1]==cell[2][0] && cell[2][0]!=0) return cell[2][1];

        //diagonal
        if(cell[1][1]==cell[2][2] && cell[1][1]==cell[0][0] && cell[1][1]!=0) return cell[1][1];
        if(cell[1][1]==cell[0][2] && cell[1][1]==cell[2][0] && cell[1][1]!=0) return cell[1][1];

        if(cell[1][1]==cell[2][1] && cell[1][1]==cell[0][1] && cell[1][1]!=0) return cell[1][1];
        if(cell[1][0]==cell[2][0] && cell[1][0]==cell[0][0] && cell[1][0]!=0) return cell[1][0];
        if(cell[1][2]==cell[2][2] && cell[1][2]==cell[0][2] && cell[1][2]!=0) return cell[1][2];

        if(emptyCells().size()==0) return DRAW;

        return IN_PROGRESS;
    }

    public ArrayList emptyCells()
    {
        ArrayList<Position> res = new ArrayList<>();
        for(int i=0;i<DEFAULT_BOARD_SIZE;i++)
        {
            for(int j=0;j<DEFAULT_BOARD_SIZE;j++)
            {
                if(cell[i][j]==0) res.add(new Position(i,j));
            }
        }
        return res;
    }

    public void printBoard()
    {
        System.out.println("--- The current board ---");
        for(int i=0;i<DEFAULT_BOARD_SIZE;i++)
        {
            System.out.print('|');
            for(int j=0;j<DEFAULT_BOARD_SIZE;j++)
            {
                if(cell[i][j]==P1) System.out.print("X");
                else if(cell[i][j]==P2) System.out.print("O");
                else System.out.print(" ");
            }
            System.out.println('|');

        }
    }
}

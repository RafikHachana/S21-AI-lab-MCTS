package com.company;


import java.util.*;

public class Main {

    public static void main(String[] args) {
	    System.out.println("Welcome to the MCTS Tic-Tac-Toe player");

	    Tree t = new Tree();

        Scanner sc = new Scanner(System.in);

        t.root.board.printBoard();
        while(true)
        {
            Node curr = t.root;
            for(int i=0;;i++)
            {
                curr.visits++;
                if(curr.board.winner!=Board.IN_PROGRESS)
                {
                    if(curr.board.winner== Board.P2)
                    {
                        System.out.println("YOU LOST :(");
                        MCTS.backPropagation(curr);
                    }
                    else if(curr.board.winner==Board.P1)   System.out.println("YOU WON !!!");
                    else System.out.println("It's a draw -_- .");
                    break;
                }
                //P1: the user
                System.out.println(i);
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
                    //curr.board.printBoard();
                }
                //P2: The MCTS algorithm
                else {
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

class Position {
    int x,y;
    public Position(int x,int y)
    {
        this.x = x;
        this.y = y;
    }
}

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
        visits = 1;
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

class MCTS {

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
        final int iterations = 2;
        for(int i=0;i<iterations;i++)
        {
            Node tmp = curr;
            /*if(curr.child.size()==0){
                MCTS.expansion(curr,Board.P2);
            }*/
            for(int turn=0;tmp.board.winner==-1;turn++)
            {
                Random rand = new Random();
                tmp.visits++;
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
            if(tmp.board.winner==Board.P2) backPropagation(tmp,curr);

        }
    }

    static void backPropagation(Node curr)
    {
        if(curr==null) return;
        curr.wins++;
        backPropagation(curr.parent);
    }

    static void backPropagation(Node curr,Node stop)
    {
        curr.wins++;
        if(curr==stop) return;
        backPropagation(curr.parent,stop);
    }
}

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

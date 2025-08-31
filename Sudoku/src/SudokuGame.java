import java.util.*;

/**
 * SudokuGame.java
 * Jogo de Sudoku para terminal.
 *
 * Compile: javac SudokuGame.java
 * Execute: java SudokuGame
 *
 * Controles (exemplos):
 *  inserir 1 3 9     -> coloca 9 na linha 1 coluna 3 (linhas/colunas de 1 a 9)
 *  limpar 1 3     -> limpa a célula linha 1 coluna 3 (se não for fixa)
 *  dica          -> revela uma célula vazia
 *  mostrar       -> imprime o tabuleiro atual
 *  verificar     -> valida se o tabuleiro está completo e correto
 *  sair          -> sai do jogo
 */
public class SudokuGame {

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        System.out.println("Sudoku — Gerando puzzle (pode demorar alguns segundos)...");
        SudokuBoard board = SudokuBoard.generatePuzzle(SudokuBoard.Difficulty.MEDIUM);

        System.out.println("Puzzle gerado. Use 'mostrar' para ver o tabuleiro. Comandos: inserir, limpar, dica, mostrar, verificar, sair.");
        board.printBoard();

        while (true) {
            System.out.print("> ");
            String line = sc.nextLine().trim();
            if (line.isEmpty()) continue;
            String[] parts = line.split("\\s+");
            String cmd = parts[0].toLowerCase();

            try {
                switch (cmd) {
                    case "inserir":
                        if (parts.length != 4) { System.out.println("Uso: inserir linha coluna valor"); break; }
                        int r = Integer.parseInt(parts[1]) - 1;
                        int c = Integer.parseInt(parts[2]) - 1;
                        int v = Integer.parseInt(parts[3]);
                        if (!board.setValue(r, c, v)) {
                            System.out.println("Jogada inválida ou célula fixa. Verifique linha/coluna/valor (1-9).");
                        } else {
                            System.out.println("Valor definido.");
                        }
                        break;
                    case "limpar":
                        if (parts.length != 3) { System.out.println("Uso: limpar a linha coluna"); break; }
                        r = Integer.parseInt(parts[1]) - 1;
                        c = Integer.parseInt(parts[2]) - 1;
                        if (!board.clearValue(r, c)) {
                            System.out.println("Não foi possível limpar (pode ser célula fixa).");
                        } else {
                            System.out.println("Célula limpa.");
                        }
                        break;
                    case "dica":
                        if (!board.revealHint()) {
                            System.out.println("Nenhuma pista disponível.");
                        } else {
                            System.out.println("Uma célula foi revelada.");
                        }
                        break;
                    case "mostrar":
                        board.printBoard();
                        break;
                    case "verificar":
                        if (board.isComplete() && board.isValidSolution()) {
                            System.out.println("Parabéns! Sudoku resolvido corretamente!");
                            board.printBoard();
                            System.exit(0);
                        } else {
                            System.out.println("Ainda não está correto ou incompleto.");
                        }
                        break;
                    case "sair":
                        System.out.println("Saindo. Até a próxima!");
                        System.exit(0);
                        break;
                    default:
                        System.out.println("Comando desconhecido. Use: inserir, limpar, dica, mostrar, verificar, sair.");
                }
            } catch (NumberFormatException ex) {
                System.out.println("Entrada numérica inválida.");
            } catch (ArrayIndexOutOfBoundsException ex) {
                System.out.println("Linha/coluna fora do intervalo (1-9).");
            }
        }
    }

    
    static class SudokuBoard {
        private final int[][] board = new int[9][9];
        private final boolean[][] fixed = new boolean[9][9]; // células iniciais que não podem ser alteradas
        private final int[][] solution = new int[9][9]; // solução completa para dicas
        private SudokuBoard() { }

        enum Difficulty {
            EASY(36), MEDIUM(46), HARD(54); // quantas células remover (aprox)
            final int removals;
            Difficulty(int removals) { this.removals = removals; }
        }

        
        static SudokuBoard generatePuzzle(Difficulty difficulty) {
            SudokuBoard sb = new SudokuBoard();
            Random rand = new Random();

         
            sb.fillSolution(rand);

        
            for (int i = 0; i < 9; i++)
                System.arraycopy(sb.board[i], 0, sb.solution[i], 0, 9);

         
            int removals = difficulty.removals;
            List<int[]> cells = new ArrayList<>();
            for (int r = 0; r < 9; r++)
                for (int c = 0; c < 9; c++)
                    cells.add(new int[] { r, c });

            Collections.shuffle(cells, rand);
            SudokuSolver solver = new SudokuSolver();

            int removed = 0;
            for (int[] pos : cells) {
                if (removed >= removals) break;
                int r = pos[0], c = pos[1];
                int backup = sb.board[r][c];
                sb.board[r][c] = 0;

              
                int count = solver.countSolutions(copyGrid(sb.board), 2);
                if (count != 1) {
                 
                    sb.board[r][c] = backup;
                } else {
                    removed++;
                }
            }

       
            for (int i = 0; i < 9; i++)
                for (int j = 0; j < 9; j++)
                    sb.fixed[i][j] = sb.board[i][j] != 0;

            return sb;
        }

        
        private void fillSolution(Random rand) {
            int[][] grid = new int[9][9];
            fillGridBacktrack(grid, 0, 0, rand);
           
            for (int i = 0; i < 9; i++)
                System.arraycopy(grid[i], 0, this.board[i], 0, 9);
        }

        private boolean fillGridBacktrack(int[][] g, int row, int col, Random rand) {
            if (row == 9) return true;
            int nextRow = col == 8 ? row + 1 : row;
            int nextCol = col == 8 ? 0 : col + 1;

            List<Integer> nums = new ArrayList<>();
            for (int i = 1; i <= 9; i++) nums.add(i);
            Collections.shuffle(nums, rand);

            for (int n : nums) {
                if (isSafe(g, row, col, n)) {
                    g[row][col] = n;
                    if (fillGridBacktrack(g, nextRow, nextCol, rand)) return true;
                    g[row][col] = 0;
                }
            }
            return false;
        }

        private boolean isSafe(int[][] g, int row, int col, int val) {
            for (int i = 0; i < 9; i++) {
                if (g[row][i] == val) return false;
                if (g[i][col] == val) return false;
            }
            int br = (row / 3) * 3;
            int bc = (col / 3) * 3;
            for (int r = br; r < br + 3; r++)
                for (int c = bc; c < bc + 3; c++)
                    if (g[r][c] == val) return false;
            return true;
        }

   
        public void printBoard() {
            System.out.println();
            for (int r = 0; r < 9; r++) {
                if (r % 3 == 0) System.out.println("+-------+-------+-------+");
                for (int c = 0; c < 9; c++) {
                    if (c % 3 == 0) System.out.print("| ");
                    if (board[r][c] == 0) System.out.print(". ");
                    else System.out.print(board[r][c] + " ");
                }
                System.out.println("|");
            }
            System.out.println("+-------+-------+-------+");
            System.out.println();
        }

        
        public boolean setValue(int row, int col, int val) {
            if (!inRange(row) || !inRange(col) || val < 1 || val > 9) return false;
            if (fixed[row][col]) return false;
           
            if (!isLegalMove(row, col, val)) return false;
            board[row][col] = val;
            return true;
        }

        public boolean clearValue(int row, int col) {
            if (!inRange(row) || !inRange(col)) return false;
            if (fixed[row][col]) return false;
            board[row][col] = 0;
            return true;
        }

        public boolean isLegalMove(int row, int col, int val) {
            
            for (int c = 0; c < 9; c++) {
                if (c != col && board[row][c] == val) return false;
            }
            for (int r = 0; r < 9; r++) {
                if (r != row && board[r][col] == val) return false;
            }
            int br = (row / 3) * 3, bc = (col / 3) * 3;
            for (int r = br; r < br + 3; r++)
                for (int c = bc; c < bc + 3; c++)
                    if (!(r == row && c == col) && board[r][c] == val) return false;
            return true;
        }

        public boolean isComplete() {
            for (int r = 0; r < 9; r++)
                for (int c = 0; c < 9; c++)
                    if (board[r][c] == 0) return false;
            return true;
        }

        public boolean isValidSolution() {
       
            for (int r = 0; r < 9; r++)
                for (int c = 0; c < 9; c++)
                    if (board[r][c] < 1 || board[r][c] > 9 || !isLegalMoveCheck(r, c, board[r][c])) return false;
            
            for (int r = 0; r < 9; r++)
                for (int c = 0; c < 9; c++)
                    if (board[r][c] != solution[r][c]) return false;
            return true;
        }

        private boolean isLegalMoveCheck(int row, int col, int val) {
            for (int c = 0; c < 9; c++) {
                if (c != col && board[row][c] == val) return false;
            }
            for (int r = 0; r < 9; r++) {
                if (r != row && board[r][col] == val) return false;
            }
            int br = (row / 3) * 3, bc = (col / 3) * 3;
            for (int r = br; r < br + 3; r++)
                for (int c = bc; c < bc + 3; c++)
                    if (!(r == row && c == col) && board[r][c] == val) return false;
            return true;
        }

        public boolean revealHint() {
            for (int r = 0; r < 9; r++)
                for (int c = 0; c < 9; c++) {
                    if (board[r][c] == 0 && !fixed[r][c]) {
                        board[r][c] = solution[r][c];
                        return true;
                    }
                }
            return false;
        }

        private static boolean inRange(int x) { return x >= 0 && x < 9; }

        private static int[][] copyGrid(int[][] g) {
            int[][] copy = new int[9][9];
            for (int i = 0; i < 9; i++) System.arraycopy(g[i], 0, copy[i], 0, 9);
            return copy;
        }
    }

    static class SudokuSolver {

        public int countSolutions(int[][] grid, int cutoff) {
            return backtrackCount(grid, 0, 0, cutoff, 0);
        }

        private int backtrackCount(int[][] g, int row, int col, int cutoff, int found) {
            if (found >= cutoff) return found;
            if (row == 9) return found + 1;
            int nextRow = col == 8 ? row + 1 : row;
            int nextCol = col == 8 ? 0 : col + 1;

            if (g[row][col] != 0) {
                return backtrackCount(g, nextRow, nextCol, cutoff, found);
            } else {
                for (int val = 1; val <= 9; val++) {
                    if (isSafe(g, row, col, val)) {
                        g[row][col] = val;
                        found = backtrackCount(g, nextRow, nextCol, cutoff, found);
                        if (found >= cutoff) {
                            g[row][col] = 0;
                            return found;
                        }
                        g[row][col] = 0;
                    }
                }
                return found;
            }
        }

        private boolean isSafe(int[][] g, int row, int col, int val) {
            for (int i = 0; i < 9; i++) {
                if (g[row][i] == val) return false;
                if (g[i][col] == val) return false;
            }
            int br = (row / 3) * 3, bc = (col / 3) * 3;
            for (int r = br; r < br + 3; r++)
                for (int c = bc; c < bc + 3; c++)
                    if (g[r][c] == val) return false;
            return true;
        }
    }
}


import java.util.*;

class Bank {
    String name;
    int netAmount;
    Set<String> types;

    Bank(String name, int netAmount, Set<String> types) {
        this.name = name;
        this.netAmount = netAmount;
        this.types = types;
    }
}

public class CashFlowMinimizer {

    private static int getMinIndex(Bank[] listOfNetAmounts) {
        int min = Integer.MAX_VALUE, minIndex = -1;
        for (int i = 0; i < listOfNetAmounts.length; i++) {
            if (listOfNetAmounts[i].netAmount == 0) continue;
            if (listOfNetAmounts[i].netAmount < min) {
                minIndex = i;
                min = listOfNetAmounts[i].netAmount;
            }
        }
        return minIndex;
    }

    private static int getSimpleMaxIndex(Bank[] listOfNetAmounts) {
        int max = Integer.MIN_VALUE, maxIndex = -1;
        for (int i = 0; i < listOfNetAmounts.length; i++) {
            if (listOfNetAmounts[i].netAmount == 0) continue;
            if (listOfNetAmounts[i].netAmount > max) {
                maxIndex = i;
                max = listOfNetAmounts[i].netAmount;
            }
        }
        return maxIndex;
    }

    private static Pair<Integer, String> getMaxIndex(Bank[] listOfNetAmounts, int minIndex, int maxNumTypes) {
        int max = Integer.MIN_VALUE;
        int maxIndex = -1;
        String matchingType = null;

        for (int i = 0; i < listOfNetAmounts.length; i++) {
            if (listOfNetAmounts[i].netAmount == 0) continue;
            if (listOfNetAmounts[i].netAmount < 0) continue;

            Set<String> intersection = new HashSet<>(listOfNetAmounts[minIndex].types);
            intersection.retainAll(listOfNetAmounts[i].types);

            if (!intersection.isEmpty() && max < listOfNetAmounts[i].netAmount) {
                max = listOfNetAmounts[i].netAmount;
                maxIndex = i;
                matchingType = intersection.iterator().next();
            }
        }
        return new Pair<>(maxIndex, matchingType);
    }

    private static void printAns(int[][] ansGraph, String[][] types, Bank[] input) {
        System.out.println("\nThe transactions for minimum cash flow are as follows : \n");
        for (int i = 0; i < ansGraph.length; i++) {
            for (int j = 0; j < ansGraph[i].length; j++) {
                if (i != j && ansGraph[i][j] != 0) {
                    System.out.println(input[i].name + " pays Rs " + ansGraph[i][j] + " to " + input[j].name + " via " + types[i][j]);
                }
            }
        }
    }

    private static void minimizeCashFlow(int numBanks, Bank[] input, Map<String, Integer> indexOf, int numTransactions, int[][] graph, int maxNumTypes) {
        Bank[] listOfNetAmounts = new Bank[numBanks];

        for (int b = 0; b < numBanks; b++) {
            listOfNetAmounts[b] = new Bank(input[b].name, 0, input[b].types);

            int amount = 0;
            for (int i = 0; i < numBanks; i++) {
                amount += graph[i][b];
            }
            for (int j = 0; j < numBanks; j++) {
                amount -= graph[b][j];
            }

            listOfNetAmounts[b].netAmount = amount;
        }

        int[][] ansGraph = new int[numBanks][numBanks];
        String[][] types = new String[numBanks][numBanks];

        int numZeroNetAmounts = (int) Arrays.stream(listOfNetAmounts).filter(bank -> bank.netAmount == 0).count();

        while (numZeroNetAmounts != numBanks) {
            int minIndex = getMinIndex(listOfNetAmounts);
            Pair<Integer, String> maxAns = getMaxIndex(listOfNetAmounts, minIndex, maxNumTypes);

            int maxIndex = maxAns.first;
            if (maxIndex == -1) {
                int simpleMaxIndex = getSimpleMaxIndex(listOfNetAmounts);
                ansGraph[minIndex][simpleMaxIndex] += Math.abs(listOfNetAmounts[minIndex].netAmount);
                types[minIndex][simpleMaxIndex] = listOfNetAmounts[minIndex].types.iterator().next();
                listOfNetAmounts[simpleMaxIndex].netAmount += listOfNetAmounts[minIndex].netAmount;
                listOfNetAmounts[minIndex].netAmount = 0;
            } else {
                int transactionAmount = Math.min(Math.abs(listOfNetAmounts[minIndex].netAmount), listOfNetAmounts[maxIndex].netAmount);
                ansGraph[minIndex][maxIndex] += transactionAmount;
                types[minIndex][maxIndex] = maxAns.second;
                listOfNetAmounts[minIndex].netAmount += transactionAmount;
                listOfNetAmounts[maxIndex].netAmount -= transactionAmount;
            }

            numZeroNetAmounts = (int) Arrays.stream(listOfNetAmounts).filter(bank -> bank.netAmount == 0).count();
        }

        printAns(ansGraph, types, input);
    }

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        System.out.println("\n\t\t\t\t********************* Welcome to CASH FLOW MINIMIZER SYSTEM ***********************\n\n\n");
        System.out.println("This system minimizes the number of transactions among multiple banks in the different corners of the world that use different modes of payment.");
        System.out.println("There is one world bank (with all payment modes) to act as an intermediary between banks that have no common mode of payment. \n\n");
        System.out.println("Enter the number of banks participating in the transactions.");
        int numBanks = sc.nextInt();

        Bank[] input = new Bank[numBanks];
        Map<String, Integer> indexOf = new HashMap<>();

        System.out.println("Enter the details of the banks and transactions as stated:");
        System.out.println("Bank name, number of payment modes it has, and the payment modes.");
        System.out.println("Bank name and payment modes should not contain spaces.");

        int maxNumTypes = 0;
        for (int i = 0; i < numBanks; i++) {
            System.out.println("Bank " + (i + 1) + " : ");
            String name = sc.next();
            int numTypes = sc.nextInt();
            Set<String> types = new HashSet<>();
            for (int j = 0; j < numTypes; j++) {
                types.add(sc.next());
            }

            if (i == 0) maxNumTypes = numTypes;
            input[i] = new Bank(name, 0, types);
            indexOf.put(name, i);
        }

        System.out.println("Enter number of transactions.");
        int numTransactions = sc.nextInt();

        int[][] graph = new int[numBanks][numBanks];

        System.out.println("Enter the details of each transaction as stated:");
        System.out.println("Debtor Bank, creditor Bank and amount");
        System.out.println("The transactions can be in any order");
        for (int i = 0; i < numTransactions; i++) {
            System.out.println(i + " th transaction : ");
            String debtor = sc.next();
            String creditor = sc.next();
            int amount = sc.nextInt();
            graph[indexOf.get(debtor)][indexOf.get(creditor)] = amount;
        }

        minimizeCashFlow(numBanks, input, indexOf, numTransactions, graph, maxNumTypes);
    }
}

class Pair<U, V> {
    public final U first;
    public final V second;

    public Pair(U first, V second) {
        this.first = first;
        this.second = second;
    }
}

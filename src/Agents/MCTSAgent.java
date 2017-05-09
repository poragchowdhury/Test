package Agents;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;

import configure.Configure;

import Agents.Agent.agentType;
import Auctioneer.Ask;
import Auctioneer.Bid;
import MCTS.Action.ACTION_TYPE;
import MCTS.MCTS;
import MCTS.TreeNode;
import Observer.Observer;
import Observer.PricePredictor;

public class MCTSAgent extends Agent {
	
	public MCTS mcts;
	public boolean firstTimeFlags =  true;
	public double remainingNeededMWh;
	
	public MCTSAgent(String name, int id, double neededMWh, double mean, double stddev, double mctsSim){
		this.playerName = name;
		this.id = id;
		this.neededMWh = neededMWh;
		this.type = agentType.BROKER;
		this.mcts = new MCTS(mctsSim, name);
	}
	
	public MCTS getMCTS(){
		return this.mcts;
	}
	
	@Override
	public void submitOrders(ArrayList<Bid> bids, ArrayList<Ask> asks, Observer observer) {
		
		////System.out.println("\nSPOT2 neededMWh " + neededMWh);
		if(firstTimeFlags){
			mcts.setup(observer);
			firstTimeFlags = false;
		}
		//long start = System.currentTimeMillis();
		observer.neededEneryMCTSBroker = this.neededMWh; 
		observer.initialNeededEneryMCTSBroker = this.initialNeededMWh;
		
		if(this.neededMWh > 0)
		{
			TreeNode bestMove = mcts.getBestMCTSMove(observer);
			if(bestMove == null)
				System.out.println("BestMove is null");
			//double minMWh = bestMove.demand / bestMove.iteration;
			//long end = System.currentTimeMillis();
			//NumberFormat formatter = new DecimalFormat("#0.00000");
			//String totalTime =  formatter.format((end - start) / 1000d); 
			//double dTotalTime =  Double.parseDouble(totalTime);
			////System.out.print("Execution time is " + formatter.format((end - start) / 1000d) + " seconds");
			//if(dTotalTime > 0){
			//	observer.nanoTime += dTotalTime;
			//	observer.nanoTimeCount++;
			//}
			//System.out.println(totalTime);
			
			//double limitPrice = bestMove.mctsClearingPrice;
			
			// Bidding configuration
			double numberofbids = 10;
			double unitPriceIncrement = 1.00;
			
			/*double [] param = new double[11];
	    	param = observer.getFeatures(param);
	    	double clearingPrice = mcts.pricePredictor.getLimitPrice(param);
	    	double limitPrice = clearingPrice + (bestMove.minMult*7.8); //tn.minmctsClearingPrice;
    		double maxPrice = clearingPrice + (bestMove.maxMult*7.8); //tn.minmctsClearingPrice;
    		double priceRange = maxPrice - limitPrice; // tn.maxmctsClearingPrice - tn.minmctsClearingPrice;
    		*/
			double limitPrice = bestMove.minmctsClearingPrice;
			double priceRange = bestMove.maxmctsClearingPrice - bestMove.minmctsClearingPrice;
			double minMWh = 1;
			
			
			unitPriceIncrement = priceRange / numberofbids;
			
			if(!bestMove.nobid){
				
				observer.mctsxPredictedCostPerHour += bestMove.currentNodeCostAvg;
				
				observer.mctsxPredictedCost[(observer.day*Configure.getHOURS_IN_A_DAY())+observer.hour][observer.hourAhead] = bestMove.totValue;
				
				if(bestMove.actionType == ACTION_TYPE.BUY){
					// Submit buy orders
					double surplus = Math.abs(this.initialNeededMWh)*(bestMove.volPercentage-1);
					double totalE = surplus + this.neededMWh;
					minMWh = totalE  / numberofbids;
					for(int i = 1; i <=numberofbids; i++){
						Bid bid = new Bid(this.playerName, this.id, limitPrice, minMWh, this.type);
						if(observer.DEBUG)
							System.out.println(bid.toString());
						bids.add(bid);
						
						limitPrice+=unitPriceIncrement;
						
					}
				}
				else{
					// Submit sell orders
					
					double surplus = 0 - neededMWh;
					minMWh= (Math.abs(this.initialNeededMWh)*(bestMove.volPercentage-1));
					if(minMWh > surplus)
						minMWh /= numberofbids;
					else
						minMWh = surplus / numberofbids;
					
					for(int i = 1; i <=numberofbids; i++){
						Ask ask = new Ask(this.playerName, this.id, limitPrice, minMWh, this.type);
						if(observer.DEBUG)
							System.out.println("Selling: " + ask.toString());
						asks.add(ask);
						
						limitPrice+=unitPriceIncrement;
					}
				}
			}
		}
	}
}

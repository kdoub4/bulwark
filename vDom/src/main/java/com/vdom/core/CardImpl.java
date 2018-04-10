package com.vdom.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.vdom.api.Card;
import com.vdom.api.GameEvent;
import com.vdom.core.MoveContext.TurnPhase;

public class CardImpl implements Card, Comparable<Card>{
	private static final long serialVersionUID = 1L;
    // Template (immutable)
    Cards.Kind kind;
    CardImpl templateCard;
    String name;
    String safeName;
    int cost;
    int debtCost;
    boolean costPotion = false;
    public ArrayList<Card> cardsUnder = new ArrayList<>();

    Cards.Kind upgradeCard = null;

    boolean isPlaceholderCard = false;

    String description = "";
    Expansion expansion = null;
    protected int vp;
    private Type[] types;
    protected int addActions;
    protected int addBuys;
    protected int addCards;
    protected int addGold;

    @Override
    public int getPileSize() {
        return pileSize;
    }

    protected int pileSize;

    @Override
    public int getSpecifiedAttacks() {
        return specifiedAttacks;
    }

    public void resetSpecifiedAttacks(){
        this.specifiedAttacks = 0;
    }
    public void setSpecifiedAttacks(int specifiedAttacks) {
        if (specifiedAttacks > this.specifiedAttacks)
            this.specifiedAttacks = specifiedAttacks;
    }

    protected int specifiedAttacks;

    @Override
    public int getBaseMight() {
        return baseMight;
    }

    public void setBaseMight(int baseMight) {
        this.baseMight = baseMight;
    }

    protected int baseMight;

    public int getAddMight() {
        return addMight;
    }

    public void setAddMight(int addMight) {
        this.addMight = addMight;
    }

    protected int addMight;

    protected int addVictoryTokens;
    boolean trashForced = false;
    boolean providePotion;
    protected int addActionsNextTurn;
    protected int addBuysNextTurn;
    protected int addCardsNextTurn;
    protected int addGoldNextTurn;
    protected boolean takeAnotherTurn;
    protected int takeAnotherTurnCardCount;
    
    boolean isOverpay = false;  // can this card be overpaid for?
    
    boolean trashOnUse = false;
    
    protected boolean callableWhenCardGained;
    protected boolean callableWhenActionResolved;
    protected boolean actionStillNeedsToBeInPlay;
    protected boolean callableWhenTurnStarts;
    protected int callableWhenCardGainedMaxCost = -1;

    protected PileCreator pileCreator = null;

    static int maxNameLen;    // across all cards
    
    // Implementation (mutable)
    private Integer id;
    boolean movedToNextTurnPile = false;
    boolean trashAfterPlay = false;
    int numberTimesAlreadyPlayed = 0;
    int cloneCount = 1;
    protected CardImpl impersonatingCard = null;
    CardImpl inheritingAbilitiesCard = null;
    private CardImpl controlCard = this;

    @Override
    public boolean isPlayedThisTurn() {
        return playedThisTurn;
    }

    @Override
    public void setPlayedThisTurn(boolean playedThisTurn) {
        if (getKind() != Cards.Kind.Prayer)
            this.playedThisTurn = playedThisTurn;
    }

    private boolean playedThisTurn = false;

    protected CardImpl(Cards.Kind kind, int cost) {
        this.kind = kind;
        this.name = kind.toString();
        if (maxNameLen < name.length()) {
            maxNameLen = name.length();
        }
        this.cost = cost;
        this.types = new Type[0];
    }
    
    public CardImpl(Builder builder) {
        this(builder.kind, builder.cost);

        name = builder.name;

        costPotion = builder.costPotion;
        debtCost = builder.debtCost;
        vp = builder.vp;
        description = builder.description;
        expansion   = builder.expansion;
        types       = builder.types != null ? builder.types : new Type[0];
        addActions       = builder.addActions;
        pileSize         = builder.pileSize;
        addBuys          = builder.addBuys;
        addCards         = builder.addCards;
        addGold          = builder.addGold;
        addVictoryTokens = builder.addVictoryTokens;
        trashForced      = builder.trashForced;
        providePotion = builder.providePotion;
        addActionsNextTurn = builder.addActionsNextTurn;
        addBuysNextTurn = builder.addBuysNextTurn;
        addCardsNextTurn = builder.addCardsNextTurn;
        addGoldNextTurn = builder.addGoldNextTurn;
        takeAnotherTurn = builder.takeAnotherTurn;
        takeAnotherTurnCardCount = builder.takeAnotherTurnCardCount;
        isOverpay   = builder.isOverpay;
        trashOnUse   = builder.trashOnUse;

        specifiedAttacks = builder.setAttacks;
        baseMight = builder.setMight;
        addMight = builder.addMight;
        upgradeCard = builder.upgradeCard;
        
        callableWhenCardGained = builder.callableWhenCardGained;
        callableWhenActionResolved = builder.callableWhenActionResolved;
        actionStillNeedsToBeInPlay = builder.actionStillNeedsToBeInPlay;
        callableWhenTurnStarts = builder.callableWhenTurnStarts;
        callableWhenCardGainedMaxCost = builder.callableWhenCardGainedMaxCost;

        pileCreator = builder.pileCreator;
    }

    public static class Builder {
        protected Cards.Kind kind;
        protected String name;
        protected int cost;
        protected int debtCost;
        protected int vp = 0;

        protected boolean costPotion = false;
        protected String description = "";
        protected Expansion expansion = null;
        protected Type[] types = null;

        protected Cards.Kind upgradeCard;

        protected int pileSize;
        protected int addActions;
        protected int setActions;
        protected int addAttacks;
        protected int setAttacks;
        protected int addBuys;
        protected int addCards;
        protected int addGold;
        protected int addMight;
        protected int setMight;
        protected int addVictoryTokens;
        protected boolean trashForced = false;
        protected boolean providePotion = false;
        protected int addActionsNextTurn;
        protected int addBuysNextTurn;
        protected int addCardsNextTurn;
        protected int addGoldNextTurn;
        protected boolean takeAnotherTurn;
        protected int takeAnotherTurnCardCount;
        
        protected boolean callableWhenCardGained;
        protected boolean callableWhenActionResolved;
        protected boolean actionStillNeedsToBeInPlay;
        protected boolean callableWhenTurnStarts;
        protected int callableWhenCardGainedMaxCost;

        protected PileCreator  pileCreator = null;
        
        protected boolean isOverpay   = false;
        
        protected boolean trashOnUse  = false;


        public Builder(Cards.Kind kind, int cost) {
            this.kind = kind;
            this.name = kind.toString();
            this.cost = cost;
        }
        
        public Builder(Cards.Kind kind, Type... type) {
            this.kind = kind;
            this.name = kind.toString();
            this.cost = 0;
            this.types = type;
        }
        
        public Builder(Cards.Kind kind, int cost, Type... type) {
            this.kind = kind;
            this.name = kind.toString();
            this.cost = cost;
            this.types = type;
        }

        public Builder(Cards.Kind kind, int cost, String name, Type... type) {
            this.kind = kind;
            this.name = name;
            this.cost = cost;
            this.types = type;
        }

        public Builder description(String val) {
            description = val;
            return this;
        }

        public Builder expansion(Expansion val) {
            expansion = val;
            return this;
        }

        public Builder costPotion() {
            costPotion = true;
            return this;
        }

        public Builder upgradeCard(Cards.Kind card  ) {
            upgradeCard = card;
            return this;
        }
        
        public Builder costDebt(int val) {
        	debtCost = val;
        	return this;
        }

        public Builder vp(int val) {
            vp = val;
            return this;
        }
        
        public Builder addActions(int val) {
            addActions = val;
            return this;
        }

        public Builder setActions(int val) {
            setActions = val;
            return this;
        }

        public Builder setAttacks(int val) {
            setAttacks = val;
            return this;
        }

        public Builder setPileSize(int count) {
            pileSize = count;
            return this;
        }


        public Builder addBuys(int val) {
            addBuys = val;
            return this;
        }

        public Builder addCards(int val) {
            addCards = val;
            return this;
        }

        public Builder addHeroism(int val) {
            addGold = val;
            return this;
        }

        public  Builder addMight(int val) {
            addMight = val;
            return this;
        }

        public  Builder setMight(int val) {
            setMight = val;
            return this;
        }


        public Builder addVictoryTokens(int val) {
            addVictoryTokens = val;
            return this;
        }

        public Builder trashForced() {
            trashForced = true;
            return this;
        }
        
        public Builder providePotion() {
            providePotion = true;
            return this;
        }
        
        public Builder addActionsNextTurn(int val) {
            addActionsNextTurn = val;
            return this;
        }

        public Builder addBuysNextTurn(int val) {
            addBuysNextTurn = val;
            return this;
        }

        public Builder addCardsNextTurn(int val) {
            addCardsNextTurn = val;
            return this;
        }

        public Builder addGoldNextTurn(int val) {
            addGoldNextTurn = val;
            return this;
        }

        public Builder takeAnotherTurn(int val) {
            takeAnotherTurn = true;
            takeAnotherTurnCardCount = val;
            return this;
        }
        
        public Builder callWhenTurnStarts() {
        	callableWhenTurnStarts = true;
            return this;
        }

        public Builder callWhenActionResolved() {
        	callableWhenActionResolved = true;
            return this;
        }
        
        public Builder callWhenActionResolved(boolean mustBeInPlay) {
        	callableWhenActionResolved = true;
        	actionStillNeedsToBeInPlay = mustBeInPlay;
            return this;
        }

        public Builder callWhenGainCard(int maxCost) {
            callableWhenCardGained = true;
            callableWhenCardGainedMaxCost = maxCost;
            return this;
        }

        public Builder isOverpay()
        {
            isOverpay = true;
            return this;
        }

        public Builder trashOnUse() {
            trashOnUse = true;
            return this;
        }

        public Builder pileCreator(PileCreator creator) {
            pileCreator = creator;
            return this;
        }

        public CardImpl build() {
        	if (expansion == null) {
        		return new CardImpl(this);
        	}
        	switch (expansion) {
        	case Base:
        		return new CardImplBase(this);
        	case Intrigue:
        		return new CardImplIntrigue(this);
        	case Seaside:
        		return new CardImplSeaside(this);
        	case Alchemy:
        		return new CardImplAlchemy(this);
        	case Prosperity:
        		return new CardImplProsperity(this);
        	case Cornucopia:
        		return new CardImplCornucopia(this);
        	case Hinterlands:
        		return new CardImplHinterlands(this);
        	case DarkAges:
        		return new CardImplDarkAges(this);
        	case Guilds:
        		return new CardImplGuilds(this);
        	case Adventures:
        		return new CardImplAdventures(this);
        	case Empires:
        		return new CardImplEmpires(this);
        	case Promo:
        		return new CardImplPromo(this);
        	default:
        		return new CardImpl(this);
        	}
        }

    }
    protected CardImpl() {
    }

    public String getSafeName() {
        return name;
    }

    public boolean isTemplateCard() {
        return templateCard == null;
    }

    public CardImpl getTemplateCard() {
        return templateCard == null ? this : templateCard;
    }

    public boolean isPlaceholderCard() {return isPlaceholderCard; }
    public void setPlaceholderCard() { isPlaceholderCard = true; }

    protected void checkInstantiateOK() {
        if (!isTemplateCard()) {
            Thread.dumpStack();
            Util.debug("Trying to create a real card from a real card instead of a template");
        }
    }

    public CardImpl instantiate() {
        checkInstantiateOK();
        CardImpl c = null;
        if (expansion == null) {
    		c = new CardImpl();
    	} else {
	    	switch (expansion) {
	    	case Base:
	    		c = new CardImplBase();
	    		break;
	    	case Intrigue:
	    		c = new CardImplIntrigue();
	    		break;
	    	case Seaside:
	    		c = new CardImplSeaside();
	    		break;
	    	case Alchemy:
	    		c = new CardImplAlchemy();
	    		break;
	    	case Prosperity:
	    		c = new CardImplProsperity();
	    		break;
	    	case Cornucopia:
	    		c = new CardImplCornucopia();
	    		break;
	    	case Hinterlands:
	    		c = new CardImplHinterlands();
	    		break;
	    	case DarkAges:
	    		c = new CardImplDarkAges();
	    		break;
	    	case Guilds:
	    		c = new CardImplGuilds();
	    		break;
	    	case Adventures:
	    		c = new CardImplAdventures();
	    		break;
	    	case Empires:
	    		c = new CardImplEmpires();
	    		break;
	    	case Promo:
	    		c = new CardImplPromo();
	    		break;
	    	default:
	    		c = new CardImpl();
	    		break;
	    	}
    	}
        copyValues(c);
        return c;
    }

    protected void copyValues(CardImpl c) {
        c.templateCard = this;
        c.id = Game.cardSequence++;

        c.upgradeCard = upgradeCard;
        c.specifiedAttacks = specifiedAttacks;
        c.baseMight = baseMight;
        c.addMight = addMight;
        c.addActions = addActions;
        c.pileSize = pileSize;

        c.kind = kind;
        c.name = name;
        c.cost = cost;
        c.costPotion = costPotion;
        c.debtCost = debtCost;
        c.description = description;
        c.expansion = expansion;
        c.types = types;
        c.providePotion = providePotion;
        c.addActions = addActions;
        c.addBuys = addBuys;
        c.addCards = addCards;
        c.addGold = addGold;
        c.addVictoryTokens = addVictoryTokens;
        c.trashForced = trashForced;
        c.addActionsNextTurn = addActionsNextTurn;
        c.addBuysNextTurn = addBuysNextTurn;
        c.addCardsNextTurn = addCardsNextTurn;
        c.addGoldNextTurn = addGoldNextTurn;
        c.takeAnotherTurn = takeAnotherTurn;
        c.takeAnotherTurnCardCount = takeAnotherTurnCardCount;
        c.isOverpay = isOverpay;
        c.vp = vp;
        c.trashOnUse = trashOnUse;
        
        c.callableWhenCardGained = callableWhenCardGained;
        c.callableWhenActionResolved = callableWhenActionResolved;
        c.callableWhenTurnStarts = callableWhenTurnStarts;
        c.callableWhenCardGainedMaxCost = callableWhenCardGainedMaxCost;
        c.actionStillNeedsToBeInPlay = actionStillNeedsToBeInPlay;
    }
    
	protected void additionalCardActions(Game game, MoveContext context, Player currentPlayer) {}

    protected void manoeuvreCardActions(Game game, MoveContext context, Player currentPlayer) {}
    

    public Cards.Kind getKind() {
        return kind;
    }

    public String getName() {
        return name;
    }

    public void setName(String newName) {
        name = newName;
    }

    @Override
    public boolean is(String ... identifier) {
        for (String id : identifier) {
            if (id != "" && getName().toLowerCase().contains(id.toLowerCase()))
                return true;
        }
        return false;
    }

    @Override
    public boolean is(String s, Type t) {
        if (is(t)) return true;
        if (is(s)) return true;
        return false;
    }

    @Override
    public boolean is(Type... types) {
        for (Type t : types) {
            if (is(t, null))
                return true;
        }
        return false;
    }
    
    @Override
    public boolean is(Type t, Player player) {
    	if (player == null || player.getInheritance() == null || !this.equals(Cards.estate)) {
            if (!behaveAsCard().equals(this)) {
                return behaveAsCard().is(t, player);
            }
	    	for (int i = 0; i < types.length; ++i) {
	    		if (types[i] == t) return true;
	    	}
	    	return false;
    	}

        if (player.getInheritance().is(t)) return true;
        for (int i = 0; i < types.length; ++i) {
            if (types[i] == t) return true;
        }
        return false;
    }

    public int getNumberOfTypes(Player player) {
        if (player == null || player.getInheritance() == null || !this.equals(Cards.estate)) {
            return types.length;
        }
        Set<Type> typeSet = new HashSet<Type>();
        typeSet.addAll(Arrays.asList(((CardImpl)player.getInheritance()).types));
        typeSet.addAll(Arrays.asList(types));
        return typeSet.size();
    }

    public int getCost(MoveContext context) {
        if (context == null)
            return cost;
        return getCost(context, context.phase == TurnPhase.Buy);
    }

    public int getCost(MoveContext context, boolean buyPhase){
        return getCost(context, buyPhase, context.phase == TurnPhase.DrawFoe);
    }

    @Override
    public boolean isInvincible(ArrayList<Card> enemyLine, MoveContext context) {
        return false;
    }

    public int getCost(MoveContext context, boolean buyPhase, boolean drawPhase) {
    	if (this.is(Type.Event, null)) return cost; //Costs of Events are not affected by cards like Bridge Troll.

        //If it's a variable card pile, and it's not empty, return the cost of the top card
        if (this.isPlaceholderCard()) {
            CardPile pile = context.game.getPile(this);
            if (!pile.isEmpty() && pile.areAllCardsVisible()) {
                return context.game.getPile(this).topCard().getCost(context, buyPhase);
            }
        }

        CardImpl controlCard = getControlCard();
        if (controlCard != null && controlCard != this && controlCard.inheritingAbilitiesCard != null) {
        	return controlCard.getCost(context, buyPhase);
        }
        
        MoveContext currentPlayerContext = context; 
        if (context.game.getCurrentPlayer() != context.getPlayer()) {
        	currentPlayerContext = new MoveContext(context.game, context.game.getCurrentPlayer());
        }
        
        int costModifier = 0;
        //TODO: BUG this isAction call for Quarry should be player-specific sometimes 
        //costModifier -= this.is(Type.Action, null) ? (2 * context.countCardsInPlay(Cards.quarry)) : 0;
        //costModifier -= context.countCardsInPlay(Cards.highway);
        //costModifier -= currentPlayerContext.countCardsInPlay(Cards.bridgeTroll);
        //costModifier -= 2 * context.countCardsInPlay(Cards.princess);
        //TODO: BUG if an inherited peddler is yours, its cost should lower during your buy phase 
        //costModifier -= (buyPhase && this.equals(Cards.peddler)) ? (2 * context.countActionCardsInPlay()) : 0;
        //costModifier -= (context.game.isPlayerSupplyTokenOnPile(this.getControlCard().equals(Cards.estate) ? this.getControlCard() : this,
        //		context.game.getCurrentPlayer(), PlayerSupplyToken.MinusTwoCost)) ? 2 : 0;
        costModifier -= this.is(Type.Location, null) ? (context.countCardsInPlay(Cards.oracularTurrent)) : 0;

        if (this.is(Type.Enemy) && !drawPhase && currentPlayerContext.game.blackMarketPile.size()>0 && this.getId()!=null)
            costModifier += isArmoured(Util.indexOfCardId(this.getId(), currentPlayerContext.game.blackMarketPile), currentPlayerContext.game.blackMarketPile);
        return Math.max(0, cost + costModifier + context.cardCostModifier/*bridge*/);
    }

    private int isArmoured(int index, ArrayList<Card> enemyLine) {
        if (index<0) return 0;
        int armourValue = 0;
        Card neighbour = null;
        if (index >= 1) {
            neighbour = enemyLine.get(index - 1);
            if (neighbour.getKind() == Cards.Kind.MgzwelGoblin){
                armourValue++;
            }
            if (is("rabble", "corpse") && neighbour.getKind() == Cards.Kind.EnshroudingMist) {
                armourValue += 2;
            }
        }
        if (index >= 2) {
            neighbour = enemyLine.get(index - 2);
            if (neighbour.getKind() == Cards.Kind.MgzwelGoblin) {
                armourValue++;
            }
        }

        if (index < enemyLine.size() - 1) {
            neighbour = enemyLine.get(index + 1);
            if (neighbour.getKind() == Cards.Kind.MgzwelGoblin){
                armourValue++;
            }
            if (is("rabble", "corpse") && neighbour.getKind() == Cards.Kind.EnshroudingMist) {
                armourValue += 2;
            }

        }
        if (index < enemyLine.size() - 2) {
            neighbour = enemyLine.get(index + 2);
            if (neighbour.getKind() == Cards.Kind.MgzwelGoblin){
                armourValue++;
            }
        }
        if (this.getKind() == Cards.Kind.LizardTroop && index > -1) {
            try {
                if (enemyLine.get(index - 1).is("Heavy")) {
                    armourValue++;
                }
            } catch (IndexOutOfBoundsException e) {

            }
            try {
                if (enemyLine.get(index + 1).is("Heavy")) {
                    armourValue++;
                }
            } catch (IndexOutOfBoundsException e) {

            }
        }
        return armourValue;
    }


    
    public int getDebtCost(MoveContext context) {
    	return debtCost;
    }
    
    @Override
    public int getVictoryPoints() {
    	return vp;
    };

    @Override
    public int getAddActions() {
        return addActions;
    }

    @Override
    public int getAddBuys() {
        return addBuys;
    }

    @Override
    public int getAddCards() {
        return addCards;
    }

    @Override
    public int getAddGold() {
        return addGold;
    }

    @Override
    public int getAddVictoryTokens() {
        return addVictoryTokens;
    }

    @Override
    public boolean trashForced() {
        return trashForced;
    }
    
    @Override
    public boolean providePotion() {
        return providePotion;
    }
    
    @Override
    public int getAddActionsNextTurn() {
        return addActionsNextTurn;
    }

    @Override
    public int getAddBuysNextTurn() {
        return addBuysNextTurn;
    }

    @Override
    public int getAddCardsNextTurn() {
        return addCardsNextTurn;
    }

    @Override
    public int getAddGoldNextTurn() {
        return addGoldNextTurn;
    }

    @Override
    public boolean takeAnotherTurn() {
        return takeAnotherTurn;
    }

    @Override
    public int takeAnotherTurnCardCount() {
        return takeAnotherTurnCardCount;
    }
    
    /**
     * @return the id
     */
    public Integer getId() {
        return id;
    }

    @Override
    public void play(Game game, MoveContext context) {
    	play(game, context, true);
    }
    
    @Override
    public void play(Game game, MoveContext context, boolean fromHand) {
    	play(game, context, fromHand, false);
    }
    
    @Override
    public void play(Game game, MoveContext context, boolean fromHand, boolean treasurePlay) {
        Player currentPlayer = context.getPlayer();
        boolean newCard = false;
        Card actualCard = (this.getControlCard() != null ? this.getControlCard() : this);
        boolean isInheritedAbility = actualCard.equals(Cards.estate) && !this.equals(actualCard);
        Card inheritedCard = this.equals(Cards.estate) ? context.player.getInheritance() : null;
        Card playedCard = isInheritedAbility ? actualCard : this;
        boolean isAction = playedCard.is(Type.Action, currentPlayer);
        boolean enchantressEffect = isAction && !context.enchantressAlreadyAffected && game.enchantressAttacks(currentPlayer);

        //manoeuvreOption Play/Manoeuvre
        if (this.is(Type.InHandManoeuvre) && !((IndirectPlayer)currentPlayer).selectBoolean(context, this))
        {
            manoeuvreCardActions(game,context,currentPlayer);
            return;
        }

        if (enchantressEffect) context.enchantressAlreadyAffected = true;
                
        if (!isInheritedAbility && isAction) {
        	context.actions += game.countChampionsInPlay(currentPlayer);
        }
        
        if (is(Type.Attack, currentPlayer))
            attackPlayed(context, game, currentPlayer);
        
        if (this.numberTimesAlreadyPlayed == 0 && this == actualCard) {
            newCard = true;
            this.movedToNextTurnPile = false;
            if (fromHand)
                currentPlayer.hand.remove(this);
            if (!enchantressEffect && trashOnUse) {
                currentPlayer.trash(this, null, context);
            } else if (!enchantressEffect && this.is(Type.Duration, currentPlayer)) {
                currentPlayer.nextTurnCards.add(this);
            } else {
                currentPlayer.playedCards.add(this);
            }
        }
        
        if (!isInheritedAbility) {
	        GameEvent event;
	        event = new GameEvent(GameEvent.EventType.PlayingCard, (MoveContext) context);
	        event.card = this;
	        event.newCard = newCard;
	        game.broadcastEvent(event);
        }
        
        if (equals(Cards.silver)) {
        	silverPlayed(context, game, currentPlayer);
        }
        
        // playing an action
        if (isAction) {
	        if (this == actualCard) 
	            context.actionsPlayedSoFar++;
	        if (!treasurePlay && context.freeActionInEffect == 0) {
	            context.actions--;
	        }
        }
        
        Card tokenPile = isInheritedAbility ? actualCard : this;
        if (game.isPlayerSupplyTokenOnPile(tokenPile, currentPlayer, PlayerSupplyToken.PlusOneAction))
        	context.actions += 1;
        if (game.isPlayerSupplyTokenOnPile(tokenPile, currentPlayer, PlayerSupplyToken.PlusOneBuy))
        	context.buys += 1;
        if (game.isPlayerSupplyTokenOnPile(tokenPile, currentPlayer, PlayerSupplyToken.PlusOneCoin))
        	context.addCoins(1);
        if (game.isPlayerSupplyTokenOnPile(tokenPile, currentPlayer, PlayerSupplyToken.PlusOneCard))
        	game.drawToHand(context, actualCard, 1 + addCards);
        
        if (enchantressEffect) {
        	//allow reaction to playing an attack card with Enchantress effect
        	if (is(Type.Attack, currentPlayer)) {
        		 for (Player player : game.getPlayersInTurnOrder()) {
    	            if (player != currentPlayer) Util.isDefendedFromAttack(game, player, this);
	            }
        	}
        	context.actions += 1;
        	game.drawToHand(context, this, 1);
        } else {
            context.setAttacks(getSpecifiedAttacks());
            context.setMight(getBaseMight());
            context.addMightModifier(getAddMight());
            context.setMelee(this.is(Type.Attack));
            context.setRange(this.is(Type.Range));
        	context.actions += addActions;
            context.buys += addBuys;
            if (this.equals(Cards.copper)) {
            	context.addCoins(addGold + context.coppersmithsPlayed);
            } else {
            	context.addCoins(addGold);
            }
            currentPlayer.addVictoryTokens(context, addVictoryTokens, this);
            if (providePotion()) {
                context.potions++;
            }
            
            for (int i = 0; i < addCards; ++i) {
            	game.drawToHand(context, this, addCards - i);
            }

            if (inheritedCard == null) {
            	additionalCardActions(game, context, currentPlayer);
            } else {
            	// Play the inheritance virtual card
            	CardImpl cardToPlay = (CardImpl) this.behaveAsCard();
		        context.freeActionInEffect++;
		        cardToPlay.play(game, context, false);
		        context.freeActionInEffect--;
            }
        }
    
        if (!isInheritedAbility && !playedCard.is(Type.Treasure, currentPlayer) || playedCard.is(Type.Action, currentPlayer)) {
        	// Don't broadcast card played event for only treasures	
        	GameEvent event;
	        event = new GameEvent(GameEvent.EventType.PlayedCard, (MoveContext) context);
	        event.card = playedCard;
	        game.broadcastEvent(event);
        } else {
        	return;
        }
        

        // test if any prince card left the play
        currentPlayer.princeCardLeftThePlay(currentPlayer);
        
        // check for cards to call after resolving action
        if (is(Type.Action)) {
	        boolean isActionInPlay = isInPlay(currentPlayer);
	        ArrayList<Card> callableCards = new ArrayList<Card>();
	        Card toCall = null;
	        for (Card c : currentPlayer.tavern) {
	        	if (c.behaveAsCard().isCallableWhenActionResolved()) {
	        		if ((c.behaveAsCard().doesActionStillNeedToBeInPlayToCall() && !isActionInPlay) ||
                            c.isPlayedThisTurn()) {
	        			continue;
	        		}
	        		callableCards.add(c);
	        	}
	        }
	        if (!callableCards.isEmpty()) {
	        	Collections.sort(callableCards, new Util.CardCostComparator());
		        do {
		        	toCall = null;
		        	// we want null entry at the end for None
		        	Card[] cardsAsArray = callableCards.toArray(new Card[callableCards.size() + 1]);
		        	//ask player which card to call
		        	toCall = currentPlayer.controlPlayer.call_whenActionResolveCardToCall(context, playedCard, cardsAsArray);
		        	if (toCall != null && callableCards.contains(toCall)) {
		        		callableCards.remove(toCall);
		        		toCall.behaveAsCard().callWhenActionResolved(context, playedCard);
		        		if (toCall.getKind() == Cards.Kind.TownSquare) {
		        		    for (int ic = callableCards.size()-1; ic >= 0; ic--) {
		        		        if (callableCards.get(ic).getKind() == Cards.Kind.TownSquare)
		        		            callableCards.remove(Cards.townSquare);
                            }
                        }
		        	}
			        // loop while we still have cards to call
			        // NOTE: we have a hack here to prevent asking for duplicate calls on an unused Royal Carriage
			        //   since technically you can ask for more and action re-played by royal carriage will ask as well
		        } while (toCall != null && !callableCards.isEmpty()); //&& toCall.equals(Cards.coinOfTheRealm)
	        }
        }
    }

    public String getStats() {
        StringBuilder sb = new StringBuilder();
        	String costString = "(" + cost + (costPotion ? "p": "") + (debtCost > 0 ? "d" + debtCost : "") + ") ";
            sb.append (costString);
        if (vp > 0) {
            sb.append(", " + vp + " victory point");
            if (vp > 1) {
                sb.append("s");
            }
        }
        if (addActions > 0 || addBuys > 0 || addCards > 0 || addGold > 0) {
            sb.append(" ");

            boolean start = true;
            if (addActions > 0) {
                if (start) {
                    start = false;
                } else {
                    sb.append(", ");
                }
                sb.append("+" + addActions + " Action");
                if (addActions > 1) {
                    sb.append("s");
                }
            }
            if (addBuys > 0) {
                if (start) {
                    start = false;
                } else {
                    sb.append(", ");
                }
                sb.append("+" + addBuys + " Buy");
                if (addBuys > 1) {
                    sb.append("s");
                }
            }
            if (addGold > 0) {
                if (start) {
                    start = false;
                } else {
                    sb.append(", ");
                }
                sb.append("+" + addGold + " Coin");
            }
            if (addCards > 0) {
                if (start) {
                    start = false;
                } else {
                    sb.append(", ");
                }
                sb.append("+" + addCards + " Card");
                if (addCards > 1) {
                    sb.append("s");
                }
            }
        }
        if (addActionsNextTurn > 0 || addBuysNextTurn > 0 || addGoldNextTurn > 0 || addCardsNextTurn > 0) {
            sb.append(" (");

            boolean start = true;
            if (addActionsNextTurn > 0) {
                if (start) {
                    start = false;
                } else {
                    sb.append(", ");
                }
                sb.append("+" + addActionsNextTurn + " Action");
                if (addActionsNextTurn > 1) {
                    sb.append("s");
                }
            }
            if (addBuysNextTurn > 0) {
                if (start) {
                    start = false;
                } else {
                    sb.append(", ");
                }
                sb.append("+" + addBuysNextTurn + " Buy");
                if (addBuysNextTurn > 1) {
                    sb.append("s");
                }
            }
            if (addGoldNextTurn > 0) {
                if (start) {
                    start = false;
                } else {
                    sb.append(", ");
                }
                sb.append("+" + addGoldNextTurn + " Coin");
            }
            if (addCardsNextTurn > 0) {
                if (start) {
                    start = false;
                } else {
                    sb.append(", ");
                }
                sb.append("+" + addCardsNextTurn + " Card");
                if (addCardsNextTurn > 1) {
                    sb.append("s");
                }
            }

            sb.append(" next turn)");
        }
        return sb.toString();
    }

    public Expansion getExpansion() {
        return expansion;
    }

    public String getDescription() {
        return description;
    }
    
    public String toString() {
        return name; // + " (id=" + id + ")";
    }

    public boolean equals(Object object) {
        return (object != null) && name.equals(((Card) object).getName()); // took away typecheck
    }
    
    public int hashCode() {
        return name.hashCode();
    }

    public boolean costPotion() {
        return costPotion;
    }
    
    public int debtCost() {
    	return debtCost;
    }

    @Override
    public Cards.Kind upgradeCard() {
        return upgradeCard;
    }

    @Override
    public boolean isOverpay(Player player) {
    	if (player == null || player.getInheritance() == null || !this.equals(Cards.estate))
    		return isOverpay;
    	return ((CardImpl)player.getInheritance()).isOverpay;
    }
    
	@Override
    public void isBuying(MoveContext context) {
		if (is(Type.Victory, context.getPlayer())) {
			context.game.trashHovelsInHandOption(context.player, context, this);
		}
		if (is(Type.Event, null)) {
			context.buys += addBuys;
			context.getPlayer().addVictoryTokens(context, addVictoryTokens, this);
		}
		if (this.equals(Cards.estate)) {
        	Card inheritance = context.getPlayer().getInheritance();
        	if (inheritance != null) {
        		inheritance.isBuying(context);
        	}
        }
    }

    @Override
    public void isBanished() {

    }

    @Override
    public void isLeavingPlay() {

    }

    @Override
    public void isBought(MoveContext context) {
    }

    @Override
    public void isDying(MoveContext context) {
        this.isLeavingPlay();
        context.attackMade = true;
        if (!this.is(Type.Range)) {
            context.meleeMade = true;
        }
        if (context.game.blackMarketPile.contains(Cards.giantCorpseHound) &&
                (!is("corpsehound"))) {
            context.game.takeWounds(context.getPlayer(),1,context,Cards.giantCorpseHound, false);
        }
        int i;
        switch (this.getKind()) {
            case BrokenCorpse:
                context.game.blackMarketPile.remove( Util.indexOfCardId(getId(), context.game.blackMarketPile));
                context.game.killACorpse(this,context.getPlayer(),context);
                context.game.blackMarketPile.add(this);
                break;
            case TheCatacombite:
                if (context.getMightTotal()==1) {
                    context.game.addToPile(this,false);
                    GameEvent event = new GameEvent(GameEvent.EventType.CardOnTopOfDeck, context);
                    context.game.broadcastEvent(event);
                }
                break;
            case KangaxxTheLich:
                for (i = context.game.blackMarketPile.size()-1; i>=0; i--) {
                    Card enemy = context.game.blackMarketPile.get(i);
                    if (enemy.is(Type.Undead) && !enemy.equals(this)) {
                        context.game.killFoe(context, enemy);
                    }
                }
                break;
            case XaphanDemon:
                context.game.takeWounds(context.getPlayer(),2,context,this, false);
                break;
            case VeporDemon:
                context.game.takeWounds(context.getPlayer(),1,context,this, false);
                break;
            case KiriElf:
                //TODO Bulwark restrictions on selection
                try {
                    for (i=0; i<2;i++) {
                        Card resurrected = context.getPlayer().kiri_onDeath(context);
                        if (resurrected != null) {
                            context.game.trashPile.remove(resurrected);
                            context.game.blackMarketPile.add(resurrected);
                        }
                    }
                }
                catch (Exception e) {

                }
                break;
            case RuihaElf:
                for (i=2; i>0; i--) {
                    if (context.game.pileSize(Cards.virtualEnemy) > 0)
                        context.game.drawFoe(context.getPlayer(), context);
                    //if enemy empty start end phases
                    if (context.game.pileSize(Cards.virtualEnemy) == 0) {
                        for (int j = 0; j < context.game.trashPile.size(); j++) {
                            Card c = context.game.trashPile.get(j);
                            if (c.is(Type.Enemy)) {
                                if (c.is(Type.Crown)) {
                                    context.game.possessedBoughtPile.add(c);
                                } else {
                                    context.game.addToPile(c, false);
                                }
                                context.game.trashPile.remove(c);
                                j--;
                            }
                        }
                        Collections.shuffle(context.game.possessedBoughtPile);
                        Collections.shuffle(context.game.getPile(Cards.virtualEnemy).cards);
                        while (!context.game.possessedBoughtPile.isEmpty()) {
                            context.game.addToPile(context.game.possessedBoughtPile.remove(0), false);
                        }
                    }
                }
                break;
        }
    }
    
    @Override
    public boolean isCallableWhenCardGained() {
    	return callableWhenCardGained;
    }
    
    @Override
    public int getCallableWhenGainedMaxCost() {
    	return callableWhenCardGainedMaxCost;
    }
    
    @Override
    public boolean isCallableWhenActionResolved() {
    	return callableWhenActionResolved;
    }
    
    @Override
    public boolean doesActionStillNeedToBeInPlayToCall() {
    	return actionStillNeedsToBeInPlay;
    }
    
    @Override
    public boolean isCallableWhenTurnStarts() {
    	return callableWhenTurnStarts;
    }
    
    @Override
    public void callWhenCardGained(MoveContext context, Card cardToGain) {	
    }
    
    @Override
    public void callWhenActionResolved(MoveContext context, Card resolvedAction) {
    }
    
    @Override
    public void callAtStartOfTurn(MoveContext context) {
    }
    
    @Override
    public void isTrashed(MoveContext context) {
        // card left play - stop any impersonations
        this.getControlCard().stopImpersonatingCard();
        this.getControlCard().stopInheritingCardAbilities();
    }

    public boolean isImpersonatingAnotherCard() {
        return !(this.impersonatingCard == null);
    }

    @Override
    public Card behaveAsCard() {
        if (impersonatingCard != null && impersonatingCard != this)
            return impersonatingCard.behaveAsCard();
        if (inheritingAbilitiesCard != null && inheritingAbilitiesCard != this)
        	return inheritingAbilitiesCard.behaveAsCard();
        return this;
    }

//    CardImpl getImpersonatingCard() {
//        return impersonatingCard;
//    }
    
    void startInheritingCardAbilities(CardImpl inheritingCard) {
    	inheritingCard.setControlCard(this);
    	this.inheritingAbilitiesCard = inheritingCard; 
    }

    void startImpersonatingCard(CardImpl impersonatingCard) {
        impersonatingCard.setControlCard(this);
        this.impersonatingCard = impersonatingCard;
    }


    void stopImpersonatingCard() {
        if (this.inheritingAbilitiesCard != null) this.inheritingAbilitiesCard.stopImpersonatingCard();
        if (this.impersonatingCard != null) this.impersonatingCard.stopImpersonatingCard();
        this.impersonatingCard = null;
    }
    
    void stopInheritingCardAbilities() {
    	this.inheritingAbilitiesCard = null;
    }

    @Override
    public CardImpl getControlCard() {
        if (controlCard == this) return this;
        return controlCard.getControlCard();
    }

    void setControlCard(CardImpl controlCard) {
        this.controlCard = controlCard;
    }
    
    protected void placeToken(MoveContext context, Card card, PlayerSupplyToken token) {
    	if (card == null) {
    		Card[] cards = context.game.getCardsInGame(GetCardsInGameOptions.Placeholders, true, Type.Action);
    		if (cards.length != 0) {
                Util.playerError(context.getPlayer(), getName() + " error: did not pick a valid pile, ignoring.");
            }
            return;
    	}
    	if (!Cards.isSupplyCard(card)) {
    		Util.playerError(context.getPlayer(), getName() + " error: Invalid pile chosen, ignoring");
    	}
    	
    	context.game.movePlayerSupplyToken(card, context.getPlayer(), token);
	}
    
    protected void attackPlayed(MoveContext context, Game game, Player currentPlayer) {
        // If an Urchin has been played, offer the player the option to trash it for a Mercenary
        for (int i = currentPlayer.playedCards.size() - 1; i >= 0 ; --i) {
            Card c = currentPlayer.playedCards.get(i);
            if (!(c.behaveAsCard() == this) && c.behaveAsCard().getKind() == Cards.Kind.Urchin && currentPlayer.controlPlayer.urchin_shouldTrashForMercenary(context, c.getControlCard())) {
                currentPlayer.trash(c.getControlCard(), this, context);
                currentPlayer.gainNewCard(Cards.mercenary, this, context);
                currentPlayer.playedCards.remove(i);
            }
        }
    }
    
    protected void silverPlayed(MoveContext context, Game game, Player player) {
    	context.silversPlayed += 1;
    	if (context.silversPlayed == 1) {
    		context.addCoins(context.merchantsPlayed);
    	}
    	int saunasInPlay = context.countCardsInPlay(Cards.sauna);
    	for (int i = 0; i < saunasInPlay; ++i) {
    		if (player.getHand().size() > 0) {
                Card cardToTrash = player.controlPlayer.sauna_cardToTrash(context);
                if (cardToTrash != null) {
                	if (!player.getHand().contains(cardToTrash)) {
                		Util.playerError(player, "Sauna error, invalid card to trash, ignoring.");
                	} else {
                		cardToTrash = player.hand.get(cardToTrash);
                		player.hand.remove(cardToTrash);
                		player.trash(cardToTrash, Cards.sauna, context);
                	}
                } else {
                	break;
                }
        	}
    	}
    }
    
    protected boolean isInPlay(Player currentPlayer) {
		for (Card c : currentPlayer.playedCards) {
			if (this == c)
				return true;
		}
		for (Card c : currentPlayer.nextTurnCards) {
			if (c instanceof CardImpl && !((CardImpl)c).trashAfterPlay && this == c)
			if (this == c)
				return true;
		}
		return false;
	}

	//Bulwark
    /*
    protected void enemyAttack(Game game, MoveContext context, Player currentPlayer) {
        if (!Util.isDefendedFromAttack(game, currentPlayer, this) {
            MoveContext targetContext = new MoveContext(game, currentPlayer);
            targetContext.attackedPlayer = currentPlayer;
            currentPlayer.gainNewCard(Cards.wound, this.getControlCard(),targetContext );
        }
    }
    */

    @Override
    public int selectAndTrashFromHand(MoveContext context, Player currentPlayer, int count) {
        Card[] cards = currentPlayer.controlPlayer.generic_cardsToTrash(context, this, count);
        if (cards != null) {
            if (cards.length != count) {
                Util.playerError(currentPlayer, this.getName() + " trash error, trying to trash too many cards, ignoring.");
            } else {
                for (Card card : cards) {
                    for (int i = 0; i < currentPlayer.hand.size(); i++) {
                        Card playersCard = currentPlayer.hand.get(i);
                        if (playersCard.equals(card)) {
                            Card thisCard = currentPlayer.hand.remove(i, false);
                            currentPlayer.trash(thisCard, this.getControlCard(), context);
                            break;
                        }
                    }
                }
            }
            return cards.length;
        }
        return 0;
    }


    protected void durationAttack(Game game, MoveContext context, Player currentPlayer) {
        for (Player targetPlayer : context.game.getPlayersInTurnOrder()) {
        	if (targetPlayer != currentPlayer) {
        		if (!Util.isDefendedFromAttack(game, targetPlayer, this)) {
        			targetPlayer.attacked(this.getControlCard(), context);
        			currentPlayer.addDurationEffectOnOtherPlayer(targetPlayer, this.kind);
        		}
        	}
        }
    }
    
    protected void witchFamiliar(Game game, MoveContext context, Player currentPlayer) {
        for (Player player : game.getPlayersInTurnOrder()) {
            if (player != currentPlayer && !Util.isDefendedFromAttack(game, player, this)) {
                player.attacked(this.getControlCard(), context);
                MoveContext targetContext = new MoveContext(game, player);
                targetContext.attackedPlayer = player;
                player.gainNewCard(Cards.curse, this.getControlCard(), targetContext);
            }
        }
    }

    protected Card throneRoomKingsCourt(Game game, MoveContext context, Player currentPlayer) {
        ArrayList<Card> actionCards = new ArrayList<Card>();
        CardImpl cardToPlay = null;
        for (Card card : currentPlayer.hand) {
            if (card.is(Type.Action, currentPlayer)) {
                actionCards.add(card);
            }
        }

        if (!actionCards.isEmpty()) {
            switch (this.kind) {
                case ThroneRoom:
                    cardToPlay = (CardImpl) currentPlayer.controlPlayer.throneRoom_cardToPlay(context);
                    break;
                case Disciple:
                    cardToPlay = (CardImpl) currentPlayer.controlPlayer.disciple_cardToPlay(context);
                    break;
                case KingsCourt:
                    cardToPlay = (CardImpl) currentPlayer.controlPlayer.kingsCourt_cardToPlay(context);
                    break;
                case Procession:
                    cardToPlay = (CardImpl) currentPlayer.controlPlayer.procession_cardToPlay(context);
                    break;
                case Crown:
                	cardToPlay = (CardImpl) currentPlayer.controlPlayer.crown_actionToPlay(context);
                    break;
                default:
                    break;
            }

            if (cardToPlay != null) {
                if(!actionCards.contains(cardToPlay)) {
                    Util.playerError(currentPlayer, this.getControlCard().name.toString() + " card selection error, ignoring");
                } else {
                    context.freeActionInEffect++;

                    cardToPlay.cloneCount = (equals(Cards.kingsCourt) ? 3 : 2);
                    int cloneCount = cardToPlay.cloneCount;
                    cardToPlay.numberTimesAlreadyPlayed = -1;
                    for (int i = 0; i < cloneCount; ++i) {
                        cardToPlay.numberTimesAlreadyPlayed++;
                        cardToPlay.play(game, context, cardToPlay.numberTimesAlreadyPlayed == 0 ? true : false);
                    }

                    cardToPlay.numberTimesAlreadyPlayed = 0;
                    context.freeActionInEffect--;
                    // If the cardToPlay was a knight, and was trashed, reset clonecount
                    if (cardToPlay.is(Type.Knight, currentPlayer) && !currentPlayer.playedCards.contains(cardToPlay) && game.trashPile.contains(cardToPlay)) {
                        cardToPlay.cloneCount = 1;
                    }

                    if (cardToPlay.is(Type.Duration, currentPlayer)) {
                    	boolean playingCardIsInNextTurn = false;
                    	for (Card c : currentPlayer.nextTurnCards) {
                    		if (c == cardToPlay) {
                    			playingCardIsInNextTurn = true;
                    			break;
                    		}
                    	}
                        // Need to move throning card to NextTurnCards first
                        // (but does not play)
                        if (playingCardIsInNextTurn && !this.getControlCard().movedToNextTurnPile) {
                            this.getControlCard().movedToNextTurnPile = true;
                            int idx = currentPlayer.playedCards.lastIndexOf(this.getControlCard());
                            int ntidx = currentPlayer.nextTurnCards.size() - 1;
                            if (idx >= 0 && ntidx >= 0) {
                                currentPlayer.playedCards.remove(idx);
                                currentPlayer.nextTurnCards.add(ntidx, this.getControlCard());
                            }
                        }
                    }
                }

                if (this.kind == Cards.Kind.Procession) {
                    if (!cardToPlay.trashOnUse) {
                        currentPlayer.trash(cardToPlay, this.getControlCard(), context);
                        if (currentPlayer.playedCards.getLastCard() == cardToPlay) { 
                            currentPlayer.playedCards.remove(cardToPlay);
                        } 
                        if (currentPlayer.nextTurnCards.contains(cardToPlay)) { 
                            ((CardImpl) cardToPlay).trashAfterPlay = true;
                        }
                    }

                    Card cardToGain = currentPlayer.controlPlayer.procession_cardToGain(context, 1 + cardToPlay.getCost(context), cardToPlay.getDebtCost(context), cardToPlay.costPotion());
                    if ((cardToGain != null) && (cardToPlay.getCost(context) + 1) == cardToGain.getCost(context) && 
                    		cardToPlay.getDebtCost(context) == cardToGain.getDebtCost(context) && 
                    		cardToPlay.costPotion() == cardToGain.costPotion()) {
                        currentPlayer.gainNewCard(cardToGain, this.getControlCard(), context);
                    }
                }
            }
        }
        return cardToPlay;
    }

    protected void multiPlayTreasure(MoveContext context, Game game, Player currentPlayer) {
    	Card treasure = null;
    	
    	switch (this.kind) {
        case Counterfeit:
            treasure = currentPlayer.controlPlayer.counterfeit_cardToPlay(context);
            break;
        case Crown:
            treasure = currentPlayer.controlPlayer.crown_treasureToPlay(context);
            break;
        default:
        	break;
    	}
        
    	if (treasure != null && treasure.is(Type.Treasure, currentPlayer) && currentPlayer.getHand().contains(treasure)) {
    		CardImpl cardToPlay = (CardImpl) treasure;
            cardToPlay.cloneCount = 2;
            for (int i = 0; i < cardToPlay.cloneCount;) {
                cardToPlay.numberTimesAlreadyPlayed = i++;
                cardToPlay.play(context.game, context, true, true);
            }
            
            cardToPlay.cloneCount = 0;
            cardToPlay.numberTimesAlreadyPlayed = 0;    		
            
            if (this.kind == Cards.Kind.Counterfeit && currentPlayer.inPlay(treasure)) {
            	if (currentPlayer.playedCards.getLastCard().equals(treasure)) {
                	currentPlayer.playedCards.remove(treasure);
                	currentPlayer.trash(treasure, this, context);
                }
            }
    	}
    }
    
    protected void discardMultiple(MoveContext context, Player currentPlayer, int numToDiscard) {
    	CardList hand = currentPlayer.getHand();
    	if (hand.size() == 0)
    		return;
    	Card[] cardsToDiscard;
    	if (hand.size() <= numToDiscard) {
    		cardsToDiscard = new Card[currentPlayer.getHand().size()];
    		for (int i = 0; i < cardsToDiscard.length; ++i) {
    			cardsToDiscard[i] = hand.get(i);
    		}
    	} else {
    		cardsToDiscard = currentPlayer.controlPlayer.discardMultiple_cardsToDiscard(context, this, numToDiscard);
            if (cardsToDiscard == null || cardsToDiscard.length != numToDiscard || !Util.areCardsInHand(cardsToDiscard, context)) {
                if (currentPlayer.hand.size() >= numToDiscard) {
                    Util.playerError(currentPlayer, "Discard error, just discarding the first " + numToDiscard + ".");
                }
                cardsToDiscard = new Card[Math.min(numToDiscard, currentPlayer.hand.size())];
                for (int i = 0; i < cardsToDiscard.length; i++) {
                    cardsToDiscard[i] = currentPlayer.hand.get(i);
                }
            }
    	}
        
        for (Card card : cardsToDiscard) {
            currentPlayer.hand.remove(card);
            currentPlayer.discard(card, this.getControlCard(), context);
        }
    }

    protected void spyAndScryingPool(Game game, MoveContext context, Player currentPlayer) {
        for (Player player : game.getPlayersInTurnOrder()) {
            // Note that this.controlCard is the opposite check of other attacks, the spy/scrying pool lets
            // the current player look at their own deck which is a good thing, so always
            // allow that
            if (player == currentPlayer || (!Util.isDefendedFromAttack(game, player, this))) {
                if (player != currentPlayer) {
                    player.attacked(this.getControlCard(), context);
                }

                MoveContext playerContext = new MoveContext(game, player);
                playerContext.attackedPlayer = player;
                Card card = game.draw(playerContext, this, 1);

                if (card != null) {
                    player.reveal(card, this.getControlCard(), playerContext);

                    boolean discard = false;

                    if(equals(Cards.spy)  || equals(Cards.stalk)) {
                        discard = currentPlayer.controlPlayer.reveal_shouldDiscard(context, player, card, this);
                    } else if (equals(Cards.scryingPool)) {
                        discard = currentPlayer.controlPlayer.scryingPool_shouldDiscard(context, player, card);
                    }

                    if (discard) {
                        player.discard(card, this.getControlCard(), playerContext);
                    } else {
                        // put it back
                        player.putOnTopOfDeck(card, playerContext, true);
                    }
                }
            }
        }

        if(equals(Cards.scryingPool)) {
            ArrayList<Card> cardsToPutInHand = new ArrayList<Card>();

            Card draw = null;
            while ((draw = game.draw(context, Cards.scryingPool, -1)) != null) {
                currentPlayer.reveal(draw, this.getControlCard(), new MoveContext(context, game, currentPlayer));
                cardsToPutInHand.add(draw);
                if(!(draw.is(Type.Action, currentPlayer))) {
                    break;
                }
            }

            for(Card card : cardsToPutInHand) {
                currentPlayer.hand.add(card);
            }
        }
    }


    public PileCreator getPileCreator() {
        if (pileCreator == null) {
            return new DefaultPileCreator();
        } else {
            return this.pileCreator;
        }
    }

    public int compareTo(Card other) {
        return getName().compareTo(other.getName());
    }

}

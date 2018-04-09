package com.vdom.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.vdom.api.Card;
import com.vdom.api.GameEvent;
import com.vdom.comms.SelectCardOptions;
import com.vdom.core.Player.SentryOption;
//test git
public class CardImplBase extends CardImpl {
    private static final long serialVersionUID = 1L;

    public CardImplBase(CardImpl.Builder builder) {
        super(builder);
    }

    protected CardImplBase() {
    }

    @Override
    public boolean isInvincible(ArrayList<Card> enemyLine, MoveContext context) {
        if (this.getKind() == Cards.Kind.KangaxxTheLich) {
            if (context.game.enemyCount(Type.Necromancer)>0) {
                return true;
            }
        }

        boolean neighborRuiha = false;
        if (this.is("elf", "demon")) {
            try {
                neighborRuiha = (enemyLine.get(Util.indexOfCardId(this.getId() - 1, enemyLine)).getKind() == Cards.Kind.RuihaElf);
            } catch (IndexOutOfBoundsException e) {

            }
            if (!neighborRuiha) {
                try {
                    neighborRuiha = (enemyLine.get(Util.indexOfCardId(this.getId() + 1, enemyLine)).getKind() == Cards.Kind.RuihaElf);
                } catch (IndexOutOfBoundsException e) {

                }
            }
            return neighborRuiha;
        }
        return false;
    }

    @Override
    protected void manoeuvreCardActions(Game game, MoveContext context, Player currentPlayer) {
        switch (this.getKind()) {
            case RalliedMilitia:
                currentPlayer.discard(currentPlayer.hand.removeCard(this), this, context);
                context.addMightModifier(+1);
                actionPhaseAttack(context,currentPlayer,true,false,1);
        }
    }
    @Override
    protected void additionalCardActions(Game game, MoveContext context, Player currentPlayer) {
        switch (this.getKind()) {
            case Watermill:
                discardMultiple(context, currentPlayer, 2);
                break;
            case TerrifiedMilitia:
                if (currentPlayer.vassal_shouldPlayCard(context, this)) {
                    currentPlayer.playedCards.remove(this);
                    game.addToPile(this,false);
                    actionPhaseAttack(context,currentPlayer, true, false, 1 );
                }
                break;
            case Belltower:
                SelectCardOptions sco = new SelectCardOptions().setPickType(SelectCardOptions.PickType.SELECT)
                        .fromBlackMarket().setCount(1).setPassable().isSelect().setCardResponsible(this);
                int[] toMoveToEnd = currentPlayer.doSelectFoe(context,sco,1, GameEvent.EventType.SelectFoe);
                if (toMoveToEnd.length==1) {
                    game.blackMarketPile.add(game.blackMarketPile.remove(toMoveToEnd[0]));
                }
                gateIncrease(context,currentPlayer,1,true);
                break;
            case Well:
                if (currentPlayer.getDeckSize() == 0 && currentPlayer.discard.size() > 0) {
                    context.game.replenishDeck(context, this, 0);
                }

                if (currentPlayer.getDeckSize() > 1) {
                    Card card = currentPlayer.peekAtDeckBottom();
                    currentPlayer.removeFromDeckBottom();
                    if (currentPlayer.controlPlayer.shouldMoveToTop(context, card, this)) {
                        currentPlayer.putOnTopOfDeck(card);
                    }
                    else {
                        currentPlayer.discard(card,card,context);
                    }

                }
                game.drawToHand(context,this,2,true);
                game.drawToHand(context,this,1,true);
                break;
            case Crypt:
                selectAndTrashFromHand(context, currentPlayer, 1);
                Card[] toDiscard = currentPlayer.controlPlayer.cardsToDiscard(context,this,1,true,false);
                if (toDiscard.length>0) {
                    currentPlayer.discard(currentPlayer.hand.removeCard(toDiscard[0]),this,context);
                }
                game.drawToHand(context,this,1,true);
                break;
            case Sidestep:
                cardsOrActions(context,currentPlayer,game,1,2, this);
                break;
            case Stalk:
                spyAndScryingPool(game,context,currentPlayer);
                break;
            case PatientHunter:
                patientHunter(game,context,currentPlayer);
                break;
            case EnchangtedBow: //intentional fall into PrestigeBow
                cellar(game,context,currentPlayer);
            case PrestigeBow:
                if (context.countCardsInPlayByIdentifier("bow")>1) {  // currentPlayer.playedCards.contains("bow")) {
                    context.setMight(2);
                }
                break;
            case DefensiveTrebuchet:
                trebuchet(context,currentPlayer);
                break;
            case GreatNorthGate:
                if (gateIncrease(context,currentPlayer,2,true)==2) {
                    context.addMightModifier(1);
                }
                break;
            case GreatEastGate:
                if (gateIncrease(context,currentPlayer,1,true)==1) {
                    context.addMightModifier(1);
                }
                break;
            case GreatWestGate:
                context.addMightModifier(gateIncrease(context,currentPlayer,3,false));
                break;
            case GreatSouthGate:
                context.addMightModifier(1);
                break;
            case SacredVault:
                this.putOnTavern(game, context, currentPlayer);
                Card[] underCards = currentPlayer.selectFromHand(context, this, 1, true, true, SelectCardOptions.ActionType.UNDER );
                if (underCards != null) {
                    Card under = underCards[0];
                    // Move to tavern mat
                    if (under.getControlCard().numberTimesAlreadyPlayed == 0) {
                        currentPlayer.hand.remove(under.getControlCard());
                        currentPlayer.tavern.add(under);
                        under.getControlCard().stopImpersonatingCard();

                        GameEvent event = new GameEvent(GameEvent.EventType.CardSetAsideOnTavernMat, context);
                        event.card = under.getControlCard();
                        game.broadcastEvent(event);
                    } else {
                        // reset clone count
                        this.getControlCard().cloneCount = 1;
                    }
                    this.cardsUnder.add(under);
                }
                break;
            case OracularTurret:
                oracularTurret(currentPlayer, context, game);
                //context.cardCostModifier -= 1;
                break;
            case Barracks:
                discardMultiple(context, currentPlayer, 1);
                break;
            case TownSquare:
            case TheDrop:
            case Orison:
            case CitadelWalls:
            case PinpointShot:
            case ContempuousShot:
            case StoneWalls:
            case VantagePoint:
                putOnTavern(game, context, currentPlayer);
                break;
            case HealingBalm:
                //TODO Bulwark multiplayer
                for (int i = 5; i > 0; i--) {
                    Card c = game.draw(context, this, i);
                    if (c.is(Type.Wound))
                        currentPlayer.trash(c, this, context);
                    else
                        currentPlayer.discard(c, this, context);
                }

                break;
            case HealingHerbs:
                //for (Player p : game.getPlayersInTurnOrder()) {
                selectAndTrashFromHand(context, currentPlayer, 1);
                //}
                break;
            case Prayer:
                putOnTavern(game, context, currentPlayer);
                break;
            case SanctusCharm:
                for (Player p : game.getPlayersInTurnOrder()) {
                    if (p != currentPlayer) {
                        //TODO may draw card
                    }
                }
                break;
            case IronShodStaff:
                cardsOrActions(context, currentPlayer, game, 2,2, this);
                int xWounds = currentPlayer.ironStaff_chooseOption(context);

                context.addMightModifier(xWounds);
                game.takeWounds(currentPlayer, xWounds, context, this, false);
                break;
            case OakStaff:
            case CarvedStaff:
                cardsOrActions(context, currentPlayer, game, 2,2, this);
                break;
            case GlancingWound:
                context.actions -= 1;
                break;
            case Armoury:
                context.travellingFairBought = true;
                break;
            case Sanitorium:
                selectAndTrashFromHand(context, currentPlayer, 2);
                game.drawToHand(context,this,1,true);
                break;
            case GuardTower:
                guardtower(context, currentPlayer);
                break;
            case RockFallTrap:
                actionPhaseAttack(context, currentPlayer, true, false, 2);
                break;
            case Attack:
            case Shield:
                shield(context, currentPlayer);
                break;
            case TowerShield:
                towershield(context, currentPlayer);
                putOnTavern(game, context, currentPlayer);
                break;
            case BloodstainedBlade:
                bloodstained(context, currentPlayer);
                break;
            case HundredScimitar:
                hunderscimitar(context, currentPlayer);
                break;
            case Charge:
                charge(context, currentPlayer);
                break;
            case Adventurer:
                adventurer(game, context, currentPlayer);
                break;
            case Artisan:
                artisan(game, context, currentPlayer);
                break;
            case Bandit:
                bandit(game, context, currentPlayer);
                break;
            case Bureaucrat:
                bureaucrat(game, context, currentPlayer);
                break;
            case Cellar:
                cellar(game, context, currentPlayer);
                break;
            case Chancellor:
                chancellor(game, context, currentPlayer);
                break;
            case Chapel:
                chapel(context, currentPlayer);
                break;
            case CouncilRoom:
                councilRoom(game, context);
                break;
            case Feast:
                feast(context, currentPlayer);
                break;
            case Harbinger:
                harbinger(game, context, currentPlayer);
                break;
            case Library:
                library(game, context, currentPlayer);
                break;
            case Merchant:
                merchant(game, context, currentPlayer);
                break;
            case Militia:
                militia(game, context, currentPlayer);
                break;
            case Mine:
                mine(context, currentPlayer);
                break;
            case Moneylender:
                moneyLender(context, currentPlayer);
                break;
            case Poacher:
                poacher(game, context, currentPlayer);
                break;
            case Remodel:
                remodel(context, currentPlayer);
                break;
            case Sentry:
                sentry(game, context, currentPlayer);
                break;
            case Spy:
                spyAndScryingPool(game, context, currentPlayer);
                break;
            case Thief:
                thief(game, context, currentPlayer);
                break;
            case ThroneRoom:
                throneRoomKingsCourt(game, context, currentPlayer);
                break;
            case Vassal:
                vassal(game, context, currentPlayer);
                break;
            case Witch:
                witchFamiliar(game, context, currentPlayer);
                break;
            case Workshop:
                workshop(currentPlayer, context);
                break;
            default:
                break;
        }
    }

    private boolean call(MoveContext context) {
        Player currentPlayer = context.getPlayer();
        if (currentPlayer.tavern.removeCard(this.getControlCard())==null) {
            return false;
        }
        currentPlayer.playedCards.add(this.getControlCard());
        GameEvent event = new GameEvent(GameEvent.EventType.CallingCard, (MoveContext) context);
        event.card = this.getControlCard();
        event.newCard = true;
        context.game.broadcastEvent(event);
        return true;
    }

    private void finishCall(MoveContext context) {
        GameEvent event = new GameEvent(GameEvent.EventType.CalledCard, (MoveContext) context);
        event.card = this.getControlCard();
        context.game.broadcastEvent(event);
    }

    public void callWhenActionResolved(MoveContext context, Card resolvedAction) {
        if (!callableWhenActionResolved) return;
        if (!call(context)) return;
        Game game = context.game;
        Player currentPlayer = context.getPlayer();
        callAction(context, currentPlayer);
        finishCall(context);
    }

    private void callTownSquare(MoveContext context, Game game, Player player) {
        int numBanished = 1;
        game.addToPile(player.playedCards.removeCard(this, true), true);
        for (Card c : player.getTavern()) {
            if (c.is("square")) {
                game.addToPile(player.getTavern().removeCard(c, true), true);
                numBanished++;
            }
        }
        for (int i = numBanished; i>0; i--) {
            game.drawToHand(context, this, numBanished);
        }
    }
    private void callPrayer(MoveContext context, Game game) {
        game.drawToHand(context, this, 2);
        game.drawToHand(context, this, 1);
    }

    private void callOrison(MoveContext context) {
        context.game.drawToHand(context, Cards.orison,1,true);
    }

    private void callSacredVault(MoveContext context, Player currentPlayer) {
        if (currentPlayer.sacredVault_chooseOption(context) == Player.SacredVaultOption.IntoHand) {
            try {
                currentPlayer.hand.add(currentPlayer.tavern.removeCard(this.cardsUnder.remove(0)));
            } catch (Exception e) {
            }
        } else {
            try {
                currentPlayer.trash(currentPlayer.tavern.removeCard(this.cardsUnder.remove(0)), this, context);
                selectAndTrashFromHand(context, currentPlayer, 1);
            } catch (Exception e) {
            }

        }
    }

    private void trebuchet(MoveContext context, Player player ){
        Card[] toTrash = player.wound_cardsToTrash(context,this,1);
        if (toTrash != null)
            if (toTrash.length>0) {
            player.hand.remove(toTrash[0]);
            player.trash(toTrash[0],this,context);
        }

        context.canAttack();
        actionPhaseAttack(context,player, true, true, 2);
        context.cannotAttack();
    }

    private int gateIncrease(MoveContext context, Player currentPlayer, int amount, boolean exact) {

        if (currentPlayer.getHand().size() == 0 ||
                (currentPlayer.getHand().size()<amount && exact))
        {
            return 0;
        }

        Card[] cardsToDiscard = currentPlayer.controlPlayer.selectFromHand(context,this,amount, exact, true, SelectCardOptions.ActionType.DISCARD );
        if (cardsToDiscard == null || !(cardsToDiscard.length == amount || !exact)) {
            return 0;
        }

        ArrayList<Card> copy = Util.copy(currentPlayer.hand);
        for (Card cardToKeep : cardsToDiscard) {
            if (!copy.remove(cardToKeep)) {
                return 0;
            }
        }

        for (Card card : cardsToDiscard) {

            currentPlayer.discard(card, this.getControlCard(), context);
            currentPlayer.hand.remove(card);
        }
        if (cardsToDiscard.length == amount || !exact)        {
            return amount;
        }
        return 0;
    }
    private void oracularTurret(Player currentPlayer, MoveContext context, Game game) {
        ArrayList<Card> cards = new ArrayList<Card>();
        for (int i = 1; i <= 3  ; i++) {
            Card card = game.takeFromPile(Cards.virtualEnemy);
            if (card == null) {
                break;
            }
            currentPlayer.reveal(card, this.getControlCard(), context);
            cards.add(card);
        }

        if (cards.size() == 0) {
            return;
        }

        Card[] orderedCards = currentPlayer.controlPlayer.topOfDeck_orderCards(context, cards.toArray(new Card[0]));

        for (int i = orderedCards.length - 1; i >= 0; i--) {
            game.addToPile(orderedCards[i], false, context);
        }

    }

    private void patientHunter(Game game, MoveContext context, Player currentPlayer) {
        ArrayList<Card> toDiscard = new ArrayList<Card>();
        int bowCardsRevealed = 0;
        Card revealed = game.draw(context, this, 1);
        if (revealed.is("bow","shot")) {
            currentPlayer.hand.add(revealed);
        }
        else {
            if (currentPlayer.reveal_shouldDiscard(context, currentPlayer, revealed, this)) {
                currentPlayer.discard(revealed,this,context);
            }
            else {
                currentPlayer.putOnTopOfDeck(revealed, context, true);
            }
        }

    }

	private void adventurer(Game game, MoveContext context, Player currentPlayer) {
        ArrayList<Card> toDiscard = new ArrayList<Card>();
        int treasureCardsRevealed = 0;

        while (treasureCardsRevealed < 2) {
            Card draw = game.draw(context, Cards.adventurer, -1);
            if (draw == null) {
                break;
            }
            currentPlayer.reveal(draw, this.getControlCard(), context);

            if (draw.is(Type.Treasure, currentPlayer)) {
                treasureCardsRevealed++;
                currentPlayer.hand.add(draw);
            } else {
                toDiscard.add(draw);
            }
        }

        while (!toDiscard.isEmpty()) {
            currentPlayer.discard(toDiscard.remove(0), this.getControlCard(), context);
        }
    }

    private void putOnTavern(Game game, MoveContext context, Player currentPlayer) {
        // Move to tavern mat
        if (this.getControlCard().numberTimesAlreadyPlayed == 0) {
            currentPlayer.playedCards.remove(currentPlayer.playedCards.lastIndexOf((Card) this.getControlCard()));
            currentPlayer.tavern.add(this.getControlCard());
            this.getControlCard().stopImpersonatingCard();
            this.setPlayedThisTurn(true);

            GameEvent event = new GameEvent(GameEvent.EventType.CardSetAsideOnTavernMat, (MoveContext) context);
            event.card = this.getControlCard();
            game.broadcastEvent(event);
        } else {
            // reset clone count
            this.getControlCard().cloneCount = 1;
        }
    }
	
	private void artisan(Game game, MoveContext context, Player currentPlayer) {
		Card card = currentPlayer.controlPlayer.artisan_cardToObtain(context);
        if (card != null) {
            if (card.getCost(context) <= 5 && card.getDebtCost(context) == 0 && !card.costPotion()) {
                currentPlayer.gainNewCard(card, this.getControlCard(), context);
            }
        }
        CardList hand = currentPlayer.getHand();
        if(hand.size() > 0) {
            Card toTopOfDeck = currentPlayer.controlPlayer.artisan_cardToReplace(context);
            if (toTopOfDeck == null || !hand.contains(toTopOfDeck)) {
                Util.playerError(currentPlayer, "No valid card selected for Artisan, returning random card to the top of the deck.");
                toTopOfDeck = Util.randomCard(hand);
            }
            for (int i = 0; i < hand.size(); ++i) {
            	Card c = hand.get(i);
            	if (c.equals(toTopOfDeck)) {
            		hand.remove(i);
            		currentPlayer.putOnTopOfDeck(c);
            		GameEvent event = new GameEvent(GameEvent.EventType.CardOnTopOfDeck, context);
                    game.broadcastEvent(event);
            		break;
            	}
            }
        }
	}
	
	private void bandit(Game game, MoveContext context, Player currentPlayer) {
		ArrayList<Player> attackedPlayers = new ArrayList<Player>();
		for (Player targetPlayer : game.getPlayersInTurnOrder()) {
            if (targetPlayer != currentPlayer && !Util.isDefendedFromAttack(game, targetPlayer, this)) {
            	attackedPlayers.add(targetPlayer);
            }
        }
		currentPlayer.gainNewCard(Cards.gold, this, context);
		for(Player targetPlayer : attackedPlayers) {
			targetPlayer.attacked(this.getControlCard(), context);
			MoveContext targetContext = new MoveContext(game, targetPlayer);
            targetContext.attackedPlayer = targetPlayer;
            ArrayList<Card> treasures = new ArrayList<Card>();
            List<Card> cardsToDiscard = new ArrayList<Card>();
            for (int i = 0; i < 2; i++) {
                Card card = game.draw(targetContext, Cards.bandit, 2 - i);
                if (card != null) {
                    targetPlayer.reveal(card, this.getControlCard(), targetContext);
                    if (card.is(Type.Treasure, targetPlayer) && !Cards.copper.equals(card)) {
                        treasures.add(card);
                    } else {
                        cardsToDiscard.add(card);
                    }
                }
            }
            
            Card cardToTrash = null;
            if (treasures.size() == 1) {
                cardToTrash = treasures.get(0);
            } else if (treasures.size() == 2) {
                if (treasures.get(0).equals(treasures.get(1))) {
                    cardToTrash = treasures.get(0);
                    cardsToDiscard.add(treasures.remove(1));
                } else {
                    cardToTrash = targetPlayer.controlPlayer.bandit_treasureToTrash(context, treasures.toArray(new Card[]{}));
                    cardToTrash = treasures.get(0).equals(cardToTrash) ? treasures.get(0) : treasures.get(1);
                    cardsToDiscard.add(treasures.get(0).equals(cardToTrash) ? treasures.get(1) : treasures.get(0));
                }
            }
            if (cardToTrash != null) {
                targetPlayer.trash(cardToTrash, this.getControlCard(), targetContext);
            }
            for (Card c: cardsToDiscard) {
                targetPlayer.discard(c, this.getControlCard(), targetContext);
            }
        }
	}
	
	private void bureaucrat(Game game, MoveContext context, Player currentPlayer) {
        currentPlayer.gainNewCard(Cards.silver, this, context);

        for (Player player : game.getPlayersInTurnOrder()) {
            if (player != currentPlayer && !Util.isDefendedFromAttack(game, player, this)) {
                player.attacked(this.getControlCard(), context);
                MoveContext playerContext = new MoveContext(game, player);
                playerContext.attackedPlayer = player;

                ArrayList<Card> victoryCards = new ArrayList<Card>();

                for (Card card : player.hand) {
                    if (card.is(Type.Victory, player)) {
                        victoryCards.add(card);
                    }
                }

                if (victoryCards.size() == 0) {
                    for (int i = 0; i < player.hand.size(); i++) {
                        Card card = player.hand.get(i);
                        player.reveal(card, this.getControlCard(), playerContext);
                    }
                } else {
                    Card toTopOfDeck = null;

                    if (victoryCards.size() == 1) {
                        toTopOfDeck = victoryCards.get(0);
                    } else if (Collections.frequency(victoryCards, victoryCards.get(0)) ==
                                victoryCards.size() /*all the same*/) {
                        toTopOfDeck = victoryCards.get(0);
                    } else {
                        toTopOfDeck = (player).controlPlayer.bureaucrat_cardToReplace(playerContext);

                        if (toTopOfDeck == null || !toTopOfDeck.is(Type.Victory, player)) {
                            Util.playerError(player, "No Victory Card selected for Bureaucrat, using first Victory Card in hand");
                            toTopOfDeck = victoryCards.get(0);
                        }
                    }

                    player.reveal(toTopOfDeck, this.getControlCard(), playerContext);
                    player.hand.remove(toTopOfDeck);
                    player.putOnTopOfDeck(toTopOfDeck);
                }
            }
        }
    }

	private void cellar(Game game, MoveContext context, Player currentPlayer) {
        Card[] cards = currentPlayer.controlPlayer.cellar_cardsToDiscard(context);
        if (cards != null) {
            int numberOfCards = 0;
            for (Card card : cards) {
                for (int i = 0; i < currentPlayer.hand.size(); i++) {
                    Card playersCard = currentPlayer.hand.get(i);
                    if (playersCard.equals(card)) {
                        currentPlayer.discard(currentPlayer.hand.remove(i), this.getControlCard(), context);
                        numberOfCards++;
                        break;
                    }
                }
            }

            if (numberOfCards != cards.length) {
                Util.playerError(currentPlayer, "Cellar discard error, trying to discard cards not in hand, ignoring extra.");
            }

            for (int i = 0; i < numberOfCards; ++i) {
            	game.drawToHand(context, this, numberOfCards - i);
            }
        }
    }

    private void attack(MoveContext context, Player currentPlayer) {
        // TODO do this.getControlCard() check at the top of the block for EVERY Util...
        if (currentPlayer.getHand().size() > 0) {
            Card card = currentPlayer.controlPlayer.action_cardToPutBackOnDeck(context, Cards.attack );

            if (card == null || !currentPlayer.hand.contains(card)) {
                Util.playerError(currentPlayer, "Courtyard error, just putting back a random card.");
                card = Util.randomCard(currentPlayer.hand);
            }

            currentPlayer.putOnTopOfDeck(currentPlayer.hand.remove(currentPlayer.hand.indexOf(card)), context, true);
        }
    }

    private void shield(MoveContext context, Player currentPlayer) {
        if (currentPlayer.getHand().size() > 0) {
            Card card = currentPlayer.controlPlayer.action_cardToPutBackOnDeck(context, this.getControlCard() );

            if (card == null || !currentPlayer.hand.contains(card)) {
                Util.playerError(currentPlayer, "Courtyard error, just putting back a random card.");
                card = Util.randomCard(currentPlayer.hand);
            }

            currentPlayer.putOnTopOfDeck(currentPlayer.hand.remove(currentPlayer.hand.indexOf(card)), context, true );
        }
    }
    public static ArrayList<Card> copy(CardList cards) {
        if (cards == null) {
            return null;
        }

        ArrayList<Card> copy = new ArrayList<Card>();
        for (Card card : cards) {
            copy.add(card);
        }

        return copy;
    }
    private void towershield(MoveContext context, Player currentPlayer) {
        if (currentPlayer.getHand().size() > 0) {
            if (currentPlayer.hand.size() > 0) {
                Card[] cards = currentPlayer.controlPlayer.action_cardsToPutOnDeck(context,this.getControlCard() , 2 );
                boolean bad = false;
                if (cards == null || cards.length > 2 || (cards.length < 2 && cards.length != currentPlayer.hand.size())) {
                    bad = true;
                } else {
                    ArrayList<Card> copy = copy(currentPlayer.hand);
                    for (Card card : cards) {
                        if (card == null || !copy.remove(card)) {
                            bad = true;
                        }
                    }
                }

                if (bad) {
                    Util.playerError(currentPlayer, "Tower Shield cards to put on deck error, putting first two cards in hand back.", false);
                    if (currentPlayer.hand.size() < 2) {
                        cards = new Card[currentPlayer.hand.size()];
                    } else {
                        cards = new Card[2];
                    }

                    for (int i = 0; i < cards.length; i++) {
                        cards[i] = currentPlayer.hand.get(i);
                    }
                }

                for (int i = cards.length - 1; i >= 0; i--) {
                    currentPlayer.putOnTopOfDeck(cards[i], context, true);
                    currentPlayer.hand.remove(cards[i]);
                }
            }
        }
    }

    private void cardsOrActions(MoveContext context, Player currentPlayer, Game game, int amountCards,
                                int amountActions, Card theCard) {
        Player.CardsOrActionsOption option = currentPlayer.controlPlayer.cardsOrActions_choose(context, theCard, amountCards , amountActions );
        if (option == null) {
            Util.playerError(currentPlayer, "Cards/Action option error, ignoring.");
        } else {
            if (option == Player.CardsOrActionsOption.AddActions) {
                context.actions += 2;
            } else if (option == Player.CardsOrActionsOption.AddCards) {
                while (amountCards > 0) {
                    game.drawToHand(context, this, amountCards);
                    amountCards--;
                }
            }
        }
    }

    private void guardtower(MoveContext context, Player currentPlayer) {
        Card topFoe = context.game.takeFromPile(Cards.virtualEnemy);

        if(topFoe != null) {
            boolean discard = currentPlayer.duchess_shouldDiscardCard(context, topFoe);
            context.game.addToPile(topFoe, discard);
        }
    }

    private void actionPhaseAttack(MoveContext context, Player currentPlayer, boolean Melee, boolean Range, int Might) {
	    //DoAttack
        int currentAttacks = context.getAttacks();
        int currentMight = context.getMight();
        boolean currentMelee = context.isMelee();
        boolean currentRange = context.isRange();
        context.setAttacks(1);
        context.setMight(Might);
        context.resetMelee();
        context.setMelee(Melee);
        context.resetRange();
        context.setRange(Range);
        context.returnToActionPhase = true;
        context.game.playerAttackFoe(currentPlayer,context);
        context.resetMight();
        context.setMight(currentMight);
        context.resetAttacks();
        context.setAttacks(currentAttacks);
        context.resetMelee();
        context.setMelee(currentMelee);
        context.resetRange();
        context.setRange(currentRange);
        context.returnToActionPhase = false;
    }

    private void bloodstained(MoveContext context, Player currentPlayer) {

    }

    private void hunderscimitar(MoveContext context, Player currentPlayer) {

    }

    private void charge(MoveContext context, Player currentPlayer) {
        context.actions = 0;
    }

    private void chancellor(Game game, MoveContext context, Player currentPlayer) {
    	if (currentPlayer.getDeckSize() == 0)
    		return;
        boolean discard = currentPlayer.controlPlayer.chancellor_shouldDiscardDeck(context);
        if (discard) {
            GameEvent event = new GameEvent(GameEvent.EventType.DeckPutIntoDiscardPile, (MoveContext) context);
            game.broadcastEvent(event);
            while (currentPlayer.getDeckSize() > 0) {
                currentPlayer.discard(game.draw(context, Cards.chancellor, 0), this.getControlCard(), null, false, false);
            }
        }
    }
	
	private void chapel(MoveContext context, Player currentPlayer) {
        Card[] cards = currentPlayer.controlPlayer.chapel_cardsToTrash(context);
        if (cards != null) {
            if (cards.length > 4) {
                Util.playerError(currentPlayer, "Chapel trash error, trying to trash too many cards, ignoring.");
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
        }
    }
	
    private void councilRoom(Game game, MoveContext context) {
        for (Player player : game.getPlayersInTurnOrder()) {
            if (player != context.getPlayer()) {
                game.drawToHand(new MoveContext(game, player), this, 1);
            }
        }
    }
	
	private void feast(MoveContext context, Player currentPlayer) {
        Card card = currentPlayer.controlPlayer.feast_cardToObtain(context);
        if (card != null) {
            // check cost
            if (card.getCost(context) <= 5 && card.getDebtCost(context) == 0 && !card.costPotion()) {
                currentPlayer.gainNewCard(card, this.getControlCard(), context);
            }
        }
    }
	
	private void harbinger(Game game, MoveContext context, Player currentPlayer) {
		if (currentPlayer.getDiscardSize() > 0)
        {
            Card card = currentPlayer.controlPlayer.cardToPutBackOnDeck(context, this);

            if (card != null) {
            	int idx = currentPlayer.discard.indexOf(card);
                if (idx >= 0) {
                	card = currentPlayer.discard.remove(idx);
                	currentPlayer.putOnTopOfDeck(card);
                	GameEvent event = new GameEvent(GameEvent.EventType.CardOnTopOfDeck, context);
                    game.broadcastEvent(event);
                } else {
                	Util.playerError(currentPlayer, "Harbinger card not in discard, ignoring.");
                }
            }
        }
	}

    private void library(Game game, MoveContext context, Player currentPlayer) {
        ArrayList<Card> toDiscard = new ArrayList<Card>();
        // only time a card is "drawn" without being directly drawn to hand
        //  we need to manually remove the minus one card token
        currentPlayer.setMinusOneCardToken(false, context);
        while (currentPlayer.hand.size() < 7) {
        	Card draw = game.draw(context, Cards.library, -1);
            if (draw == null) {
                break;
            }
            
            boolean shouldKeep = true;
            if (draw.is(Type.Action, currentPlayer)) {
                shouldKeep = currentPlayer.controlPlayer.library_shouldKeepAction(context, draw);
            }

            if (shouldKeep) {
                currentPlayer.hand.add(draw);
            } else {
                toDiscard.add(draw);
            }
        }

        while (!toDiscard.isEmpty()) {
            currentPlayer.discard(toDiscard.remove(0), this.getControlCard(), null);
        }
    }
    
    private void merchant(Game game, MoveContext context, Player currentPlayer) {
        context.merchantsPlayed++;
    }
    
    private void militia(Game game, MoveContext context, Player currentPlayer) {
        for (Player player : game.getPlayersInTurnOrder()) {
            if (player != currentPlayer && !Util.isDefendedFromAttack(game, player, this)) {
                player.attacked(this.getControlCard(), context);
                MoveContext playerContext = new MoveContext(game, player);
                playerContext.attackedPlayer = player;

                int keepCardCount = 3;
                if (player.hand.size() > keepCardCount) {
                    Card[] cardsToKeep = player.controlPlayer.militia_attack_cardsToKeep(playerContext);
                    player.discardRemainingCardsFromHand(playerContext, cardsToKeep, this.getControlCard(), keepCardCount);
                }
            }
        }
    }
    
    private void mine(MoveContext context, Player currentPlayer) {
        Card cardToUpgrade = currentPlayer.controlPlayer.mine_treasureFromHandToUpgrade(context);
        if ((Game.errataMineForced && cardToUpgrade == null) || !cardToUpgrade.is(Type.Treasure, currentPlayer)) {
            Card[] cards = currentPlayer.getTreasuresInHand().toArray(new Card[] {});
            if (cards.length != 0) {
                Util.playerError(currentPlayer, "Mine card to upgrade was invalid, picking treasure from hand.");
                cardToUpgrade = Util.randomCard(cards);
            }
        }

        if (cardToUpgrade != null) {
            CardList hand = currentPlayer.getHand();
            for (int i = 0; i < hand.size(); i++) {
                Card card = hand.get(i);

                if (cardToUpgrade.equals(card)) {
                    Card thisCard = currentPlayer.getHand().remove(i);
                    currentPlayer.trash(thisCard, this.getControlCard(), context);

                    Card newCard = currentPlayer.controlPlayer.mine_treasureToObtain(context, card.getCost(context) + 3, card.getDebtCost(context), card.costPotion());
                    if (!(newCard != null && newCard.is(Type.Treasure, null) && Cards.isSupplyCard(newCard) &&
                    		newCard.getCost(context) <= card.getCost(context) + 3 && 
                    		newCard.getDebtCost(context) <= card.getDebtCost(context) && 
                    		(!newCard.costPotion() || card.costPotion()) 
                    		&& context.isCardOnTop(newCard))) {
                        Util.playerError(currentPlayer, "Mine treasure to obtain was invalid, picking random treasure from table.");
                        for (Card treasureCard : context.getCardsInGame(GetCardsInGameOptions.TopOfPiles, true, Type.Treasure)) {
                            if (Cards.isSupplyCard(treasureCard) && context.isCardOnTop(treasureCard) &&
                            		treasureCard.getCost(context) <= card.getCost(context) + 3 &&
                            		treasureCard.getDebtCost(context) <= card.getCost(context) &&
                            		(!treasureCard.costPotion() || card.costPotion()))
                                newCard = treasureCard;
                        }
                    }

                    if (newCard != null && newCard.getCost(context) <= card.getCost(context) + 3 && Cards.isSupplyCard(newCard) && context.isCardOnTop(newCard))
                        currentPlayer.gainNewCard(newCard, this.getControlCard(), context);
                    break;
                }
            }
        }
    }

    private void moneyLender(MoveContext context, Player currentPlayer) {
        for (int i = 0; i < currentPlayer.hand.size(); i++) {
            Card card = currentPlayer.hand.get(i);
            if (card.equals(Cards.copper)) {
            	boolean choseTrash = false;
            	if (Game.errataMoneylenderForced || (choseTrash = currentPlayer.controlPlayer.moneylender_shouldTrashCopper(context))) {
	                Card thisCard = currentPlayer.hand.remove(i);
	                context.addCoins(3);
	                currentPlayer.trash(thisCard, this.getControlCard(), context);
	                break;
            	}
            	if (!choseTrash)
            		break;
            }
        }
    }
    
    private void poacher(Game game, MoveContext context, Player currentPlayer) {
    	if (currentPlayer.getHand().isEmpty()) return;
    	int numToDiscard = game.emptyPiles();
    	if (numToDiscard == 0) return;
    	CardList hand = currentPlayer.getHand(); 
    	Card[] cards = null;
    	if (hand.size() <= numToDiscard) {
    		cards = hand.toArray();
    	} else {
    		cards = currentPlayer.controlPlayer.poacher_cardsToDiscard(context, numToDiscard);
    		if (!(cards != null && cards.length == numToDiscard && Util.areCardsInHand(cards, context))) {
    			Util.playerError(currentPlayer, "Poacher discard error, picking first cards");
    			cards = new Card[numToDiscard];
    			for (int i = 0; i < numToDiscard; ++i) {
    				cards[i] = hand.get(i);
    			}
    		}
    	}
        for (Card card : cards) {
            for (int i = 0; i < currentPlayer.hand.size(); i++) {
                Card playersCard = currentPlayer.hand.get(i);
                if (playersCard.equals(card)) {
                    currentPlayer.discard(currentPlayer.hand.remove(i), this.getControlCard(), context);
                    break;
                }
            }
        }
    }
    
    private void remodel(MoveContext context, Player currentPlayer) {
        if(currentPlayer.getHand().size() > 0) {
            Card cardToTrash = currentPlayer.controlPlayer.remodel_cardToTrash(context);

            if (cardToTrash == null) {
                Util.playerError(currentPlayer, "Remodel did not return a card to trash, trashing random card.");
                cardToTrash = Util.randomCard(currentPlayer.getHand());
            }

            int cost = -1;
            int debt = -1;
            boolean potion = false;
            for (int i = 0; i < currentPlayer.hand.size(); i++) {
                Card playersCard = currentPlayer.hand.get(i);
                if (playersCard.equals(cardToTrash)) {
                    cost = playersCard.getCost(context);
                    debt = playersCard.getDebtCost(context);
                    potion = playersCard.costPotion();
                    playersCard = currentPlayer.hand.remove(i);

                    currentPlayer.trash(playersCard, this.getControlCard(), context);
                    break;
                }
            }

            if (cost == -1) {
                Util.playerError(currentPlayer, "Remodel returned invalid card, ignoring.");
                return;
            }

            cost += 2;

            Card card = currentPlayer.controlPlayer.remodel_cardToObtain(context, cost, debt, potion);
            if (card != null) {
                // check cost
                if (card.getCost(context) > cost) {
                    Util.playerError(currentPlayer, "Remodel new card costs too much, ignoring.");
                }
                else if (card.getDebtCost(context) > debt) {
                    Util.playerError(currentPlayer, "Remodel new card costs too much debt, ignoring.");
                } else if (card.costPotion() && !potion) {
                    Util.playerError(currentPlayer, "Remodel new card costs potion, ignoring.");
                } else {
                    if (currentPlayer.gainNewCard(card, this.getControlCard(), context) == null) {
                        Util.playerError(currentPlayer, "Remodel new card is invalid, ignoring.");
                    }
                }
            }
        }
    }
    
    private void sentry(Game game, MoveContext context, Player currentPlayer) {
    	ArrayList<Card> topOfTheDeck = new ArrayList<Card>();
        for (int i = 0; i < 2; i++) {
            Card card = game.draw(context, Cards.sentry, 2 - i);
            if (card != null) {
                topOfTheDeck.add(card);
            }
        }

        ArrayList<Card> toTrash = new ArrayList<Card>();
        ArrayList<Card> toDiscard = new ArrayList<Card>();
        ArrayList<Card> toReplace = new ArrayList<Card>();
        if (topOfTheDeck.size() > 0) {
        	for (Card c : topOfTheDeck) {
        		SentryOption option = currentPlayer.controlPlayer.sentry_chooseOption(context, c, topOfTheDeck.toArray(new Card[topOfTheDeck.size()]));
        		if (option == null) {
        			Util.playerError(currentPlayer, "Sentry chose null option - trashing card");
        			option = SentryOption.Trash;
        		}
        		switch (option) {
	        		case Trash:
	        			toTrash.add(c);
	        			break;
	        		case Discard:
	        			toDiscard.add(c);
	        			break;
	        		case PutBack:
	        			toReplace.add(c);
	        			break;
	        		default:
	        			toTrash.add(c);
	        			break;
        		}
        	}
        	for (Card c : toTrash) {
        		currentPlayer.trash(c, this.getControlCard(), context);
        	}
        	for (Card c : toDiscard) {
        		currentPlayer.discard(c, this.getControlCard(), context);
        	}
        	if (toReplace.size() > 0) {
	        	Card[] order;
	            if(toReplace.size() == 1) {
	                order = toReplace.toArray(new Card[toReplace.size()]);
	            } else {
	                order = currentPlayer.controlPlayer.sentry_cardOrder(context, toReplace.toArray(new Card[toReplace.size()]));
	                if (!Util.areCardsInList(order, toReplace)) {
	                	Util.playerError(currentPlayer, "Sentry order cards error, ignoring.");
	                	order = toReplace.toArray(new Card[toReplace.size()]);
	                }
	            }
	
	            // Put the cards back on the deck
	            for (int i = order.length - 1; i >= 0; i--) {
	                currentPlayer.putOnTopOfDeck(order[i]);
	            }
        	}
        }
    }
    
    private void thief(Game game, MoveContext context, Player currentPlayer) {
        ArrayList<Card> trashed = new ArrayList<Card>();

        for (Player targetPlayer : game.getPlayersInTurnOrder()) {
            if (targetPlayer != currentPlayer && !Util.isDefendedFromAttack(game, targetPlayer, this)) {
                targetPlayer.attacked(this.getControlCard(), context);
                MoveContext targetContext = new MoveContext(game, targetPlayer);
                targetContext.attackedPlayer = targetPlayer;
                ArrayList<Card> treasures = new ArrayList<Card>();

                List<Card> cardsToDiscard = new ArrayList<Card>();
                for (int i = 0; i < 2; i++) {
                    Card card = game.draw(targetContext, Cards.thief, 2 - i);

                    if (card != null) {
                        targetPlayer.reveal(card, this.getControlCard(), targetContext);

                        if (card.is(Type.Treasure, targetPlayer)) {
                            treasures.add(card);
                        } else {
                            cardsToDiscard.add(card);
                        }
                    }
                }

                for (Card c: cardsToDiscard) {
                    targetPlayer.discard(c, this.getControlCard(), targetContext);
                }

                Card cardToTrash = null;

                if (treasures.size() == 1) {
                    cardToTrash = treasures.get(0);
                } else if (treasures.size() == 2) {
                    if (treasures.get(0).equals(treasures.get(1))) {
                        cardToTrash = treasures.get(0);
                        targetPlayer.discard(treasures.remove(1), this.getControlCard(), targetContext);
                    } else {
                        cardToTrash = currentPlayer.controlPlayer.thief_treasureToTrash(context, treasures.toArray(new Card[] {}));
                    }

                    for (Card treasure : treasures) {
                        if (!treasure.equals(cardToTrash)) {
                            targetPlayer.discard(treasure, this.getControlCard(), targetContext);
                        }
                    }
                }

                if (cardToTrash != null) {
                    targetPlayer.trash(cardToTrash, this.getControlCard(), targetContext);
                    trashed.add(cardToTrash);
                }
            }
        }

        if (trashed.size() > 0) {
            Card[] treasuresToGain = currentPlayer.controlPlayer.thief_treasuresToGain(context, trashed.toArray(new Card[] {}));

            if (treasuresToGain != null) {
                for (Card treasure : treasuresToGain) {
                    currentPlayer.gainCardAlreadyInPlay(treasure, this.getControlCard(), context);
                    game.trashPile.remove(treasure);
                }
            }
        }
    }
    
    private void vassal(Game game, MoveContext context, Player player) {
    	Card draw = game.draw(context, Cards.warrior, 1);
        if (draw != null) {
        	player.discard(draw, this.getControlCard(), context);
        	int discardIndex = player.discard.size() - 1;
        	if (draw.is(Type.Action, player) && player.controlPlayer.vassal_shouldPlayCard(context, draw)) {
        		//TODO: doesn't apply with current cards, 
        		//      but future card could trigger lose track rule and prevent moving to play area
        		//      but not prevent from playing
        		player.discard.remove(discardIndex);
        		context.freeActionInEffect++;
                draw.play(game, context, false);
                context.freeActionInEffect--;
        	}
        }
    }

    private void workshop(Player currentPlayer, MoveContext context) {
        Card card = currentPlayer.controlPlayer.workshop_cardToObtain(context);
        if (card != null) {
            // check cost
            if (card.getCost(context) <= 4 && card.getDebtCost(context) == 0 && !card.costPotion()) {
                currentPlayer.gainNewCard(card, this.getControlCard(), context);
            }
        }
    }

    public void callAtStartOfTurn(MoveContext context) {
        if (!callableWhenTurnStarts) return;
        if (!call(context)) return;
        Player currentPlayer = context.getPlayer();
        callAction(context, currentPlayer);
        finishCall(context);
    }

    private void callAction(MoveContext context, Player currentPlayer) {
        switch (this.getKind()) {
            case TownSquare:
                callTownSquare(context, context.game, currentPlayer);
                break;
            case Prayer:
                callPrayer(context, context.game );
                break;
            case SacredVault:
                callSacredVault(context,currentPlayer);
                break;
            case Orison:
                callOrison(context);
                break;
        }
    }
}

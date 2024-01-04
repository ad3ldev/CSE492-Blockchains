// SPDX-License-Identifier: GPL-3.0
// I acknowledge that I am aware of the academic integrity guidelines of this course, and that I worked on this assignment independently without any unauthorized help
// عبد الرحمن عادل عبد الفتاح
pragma solidity ^0.8.4;
contract RockPaperScissors {
    struct Participant{
        address pAddress;
        bytes32 choice_hash;
        Moves choice;
        string salt;
    }

    address public manager;
    Participant public participantA;
    Participant public participantB;
    uint public reward;

    address public winner;
    uint public remainingReward;

    enum GameOutcome { None, ParticipantA, ParticipantB, Tie}
    enum Moves {None, Rock, Paper, Scissors}

    GameOutcome public gameOutcome = GameOutcome.None;

    modifier onlyManager(){
        require(msg.sender == manager, "Only manager can call this function");
        _;
    }

    modifier onlyParticipants(){
        require(msg.sender == participantA.pAddress || msg.sender == participantB.pAddress, "Only Participants can call this function");
        _;
    }

    modifier choicesSumbitted(){
        require(participantA.choice_hash[0] != 0 && participantB.choice_hash[0] != 0, "Both participants must submit their choices");
        _;
    }
    modifier choicesRevealed(){
        require(participantA.choice != Moves.None && participantB.choice != Moves.None, "Both participants must reveal their choices");
        _;
    }

    constructor(address _participantA, address _participantB){
        manager = msg.sender;
        participantA.pAddress = _participantA;
        participantB.pAddress = _participantB;
        participantA.choice = Moves.None;
        participantB.choice = Moves.None;
        reward = 0;
    }

    function depositReward() external payable onlyManager{
        require(msg.value > 0, "Reward must be greater than 0");
        reward += msg.value;
    }
    function commitChoice(Moves choice, string memory salt) external onlyParticipants{
        require(participantA.choice_hash[0] == 0 || participantB.choice_hash[0] == 0, "Choices already submitted");
        if (msg.sender == participantA.pAddress){
            require(participantA.choice_hash[0] == 0, "Choice of Participant A already done");
            participantA.salt = salt;
            participantA.choice_hash = keccak256(abi.encode(choice, salt));
        }else{
            require(participantB.choice_hash[0] == 0, "Choice of Participant B already done");
            participantB.salt = salt;
            participantB.choice_hash = keccak256(abi.encode(choice, salt));
        }
    }
    function reveal(Moves choice) external onlyParticipants choicesSumbitted{
        bytes32 hashedChoice;
        if (msg.sender == participantA.pAddress){
            hashedChoice = keccak256(abi.encode(choice, participantA.salt));
            require(hashedChoice == participantA.choice_hash, "Invalid Choice for Participant 1");
            participantA.choice = choice;
        }else{
            hashedChoice = keccak256(abi.encode(choice, participantB.salt));
            require(hashedChoice == participantB.choice_hash, "Invalid Choice for Participant 2");
            participantB.choice = choice;
        }
        if(participantA.choice != Moves.None && participantB.choice != Moves.None){
            determineWinner();
        }
    }
    function determineWinner() internal choicesRevealed {
        if(participantA.choice == participantB.choice){
            gameOutcome = GameOutcome.Tie;
        }else if(
            (participantA.choice == Moves.Paper && participantB.choice == Moves.Rock) &&
            (participantA.choice == Moves.Rock && participantB.choice == Moves.Scissors) &&
            (participantA.choice == Moves.Scissors && participantB.choice == Moves.Paper)
        ){
            gameOutcome =GameOutcome.ParticipantA;
            winner = participantA.pAddress;
        }else{
            gameOutcome =GameOutcome.ParticipantB;
            winner = participantB.pAddress;
        }
        distributeReward();
    }
    function distributeReward() internal{
        if (gameOutcome == GameOutcome.Tie){
            remainingReward = reward;
        }else{
            remainingReward = 0;
            payable(winner).transfer(reward);
        }
    }
    function claimReward() external onlyParticipants choicesRevealed{
        require(gameOutcome == GameOutcome.Tie, "Only when there is a tie");
        require(remainingReward > 0, "No remaining reward to claim");

        uint participantShare = remainingReward / 2;
        payable(participantA.pAddress).transfer(participantShare);
        payable(participantB.pAddress).transfer(participantShare);

        remainingReward = 0;
    }
}   

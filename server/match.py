class Match:
    def __init__(self, host, goal_tracker):
        self.player1 = host
        self.player2 = None
        self.started = False 
        self.goal_tracker = goal_tracker
        self.id = None
    
    def match_is_full(self):
        return self.player2 is not None
    
    def is_player_host(self, player):
        return player == self.player1
    
    def is_player_guest(self, player):
        return player == self.player2
    
    def is_player_in_match(self, player):
        return player == self.player1 or player == self.player2
    
    def quit_player(self, player):
        if self.is_player_host(player):
            self.player1 = self.player2
            self.player2 = None
        elif self.is_player_guest(player):
            self.player2 = None
        else:
            raise ValueError("Player not in match")
    
    def match_is_empty(self):
        return self.player2 is None and self.player1 is None
    
    def add_player(self, player):
        if self.player2 is not None:
            raise ValueError("Match is full")
        
        if self.player1 is None:
            raise ValueError("Empty match should not exist")
        
        self.player2 = player
        
    def start_match(self, id):
        if self.player2 is None or self.player1 is None:
            raise ValueError("Match is not full")
        
        self.started = True
        self.id = id
    
    def match_has_started(self):
        return self.started
    
    def get_players(self):
        return [self.player1, self.player2]
    
    def get_host(self):
        return self.player1
    
    def get_guest(self):
        return self.player2
        
    def get_game_state(self):
        if not self.match_has_started():
            raise ValueError("Match has not been started")
        else:
            return self.goal_tracker.get_target_coords_with_scorers()
    
    def get_goals(self):
        return self.goal_tracker.get_target_coords()
    
    def report_position(self, player, lon, lat):
        if not self.match_has_started():
            raise ValueError("Match has not been started")
        
        self.goal_tracker.report_position(player, lon, lat)
        
    def get_match_id(self):
        return self.id


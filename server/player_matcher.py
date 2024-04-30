from goal_tracker import get_goal_tracker_with_simple_goals
from match import Match

class PlayerMatcher:
    def __init__(self):
        self.match_list = []
    
    def get_matches(self):
        return [match.get_players() for match in self.match_list]
    
    def get_matches_that_have_not_started(self):
        return [match.get_players() for match in self.match_list if not match.match_has_started()]
    
    def get_open_matches(self):
        return [match.get_players() for match in self.match_list if not match.match_is_full() and not match.match_has_started()]        
    
    # This should return either [] or a single match
    # More than one match means that something went terribly wrong
    def get_matches_hosted_by(self, player):
        matches = [match for match in self.match_list if match.is_player_host(player)]
        
        if len(matches) > 1:
            raise ValueError("Player should not be hosting more than one match")
        
        return matches
    
    def get_matches_guested_by(self, player):
        matches = [match for match in self.match_list if match.is_player_guest(player)]
        
        if len(matches) > 1:
            raise ValueError("Player should not be guesting more than one match")
        
        return matches
    
    def is_player_in_match(self, player):
        return any([match.is_player_in_match(player) for match in self.match_list])
    
    def can_participate_in_match(self, player):
        return not self.is_player_in_match(player)
    
    def can_join_match(self, host, player):
        if not self.can_participate_in_match(player):
            return False
        
        matches = self.get_matches_hosted_by(host)
        
        if len(matches) > 1:
            raise ValueError("Player should not be hosting more than one match")
        
        if len(matches) == 0:
            return False 
        
        return not matches[0].match_is_full()
        
    def create_match(self, player):
        if not self.can_participate_in_match(player):
            raise ValueError("Player is already in a match")
        
        match = Match(player, get_goal_tracker_with_simple_goals())
        self.match_list.append(match)
        
    def join_match(self, host, player):
        if not self.can_join_match(host, player):
            raise ValueError("Player cannot join match")
        
        match = self.get_matches_hosted_by(host)[0]
        match.add_player(player)
    
    def can_leave_match(self, player):
        return self.is_player_in_match(player) and not self.is_match_started(player)
    
    def leave_match(self, player):
        matches = [match for match in self.match_list if match.is_player_in_match(player)]
        
        if len(matches) > 1:
            raise ValueError("Player should not be in more than one match")
        
        if len(matches) == 0:
            raise ValueError("Player is not in any match")
        
        matches[0].quit_player(player)
        
        if matches[0].match_is_empty():
            self.match_list.remove(matches[0])
          
    def can_game_be_started(self, host):
        matches = self.get_matches_hosted_by(host)
        
        if len(matches) > 1:
            raise ValueError("Player should not be hosting more than one match")
        
        if len(matches) == 0:
            return False 
        
        return matches[0].match_is_full() and not matches[0].match_has_started()
      
    def start_match(self, host, id):
        if not self.can_game_be_started(host):
            raise ValueError("Match cannot be started")
        
        matches = self.get_matches_hosted_by(host)
        
        matches[0].start_match(id)
    
    def get_state_for_match(self, host):
        matches = self.get_matches_hosted_by(host) + self.get_matches_guested_by(host)
        
        if len(matches) > 1:
            raise ValueError("Player should not be hosting more than one match")
        
        if len(matches) == 0:
            return None 
        
        return matches[0].get_game_state()
    
    def get_coords_for_match(self, player):
        matches = self.get_matches_hosted_by(player) + self.get_matches_guested_by(player)
        
        if len(matches) > 1:
            raise ValueError("Player is in more than one match")
        
        if len(matches) == 0:
            return None
        
        return matches[0].get_goals()
    
    def report_position_of(self, player, lon, lat):
        matches = self.get_matches_hosted_by(player) + self.get_matches_guested_by(player)
        
        if len(matches) > 1:
            raise ValueError("Player has his position reported in more than one match")
        
        if len(matches) == 0:
            raise ValueError("Player is not in any match")
        
        matches[0].report_position(player, lon, lat)
        
    def is_match_started(self, player):
        matches = self.get_matches_hosted_by(player) + self.get_matches_guested_by(player)
        
        if len(matches) > 1:
            raise ValueError("Player is in more than one match")
        
        if len(matches) == 0:
            return False
        
        return matches[0].match_has_started()
    
    def get_opponent(self, player):
        matches = self.get_matches_hosted_by(player) + self.get_matches_guested_by(player)
        
        if len(matches) > 1:
            raise ValueError("Player is in more than one match")
        
        if len(matches) == 0:
            return None
        
        return matches[0].get_opponent(player)
    
    def get_player_list(self, player):
        matches = self.get_matches_hosted_by(player) + self.get_matches_guested_by(player)
        
        if len(matches) > 1:
            raise ValueError("Player is in more than one match")
        
        if len(matches) == 0:
            return None
        
        return matches[0].get_players()
    
    def set_game_id_for_match(self, player, id):
        matches = self.get_matches_hosted_by(player) + self.get_matches_guested_by(player)
        
        if len(matches) > 1:
            raise ValueError("Player is in more than one match")
        
        if len(matches) == 0:
            raise ValueError("Player is not in any match")
        
        matches[0].set_game_id(id)
        
    def get_match_id(self, player):
        matches = self.get_matches_hosted_by(player) + self.get_matches_guested_by(player)
        
        if len(matches) > 1:
            raise ValueError("Player is in more than one match")
        
        if len(matches) == 0:
            return None
        
        return matches[0].get_match_id()

    def get_goals(self, player):
        matches = self.get_matches_hosted_by(player) + self.get_matches_guested_by(player)
        
        if len(matches) > 1:
            raise ValueError("Player is in more than one match")
        
        if len(matches) == 0:
            return None
        
        return matches[0].get_goal_objs()
    
    def get_winner(self, player):
        matches = self.get_matches_hosted_by(player) + self.get_matches_guested_by(player)
        
        if len(matches) > 1:
            raise ValueError("Player is in more than one match")
        
        if len(matches) == 0:
            return None
        
        return matches[0].get_winner()

def main():
    m = PlayerMatcher()
    
    m.create_match("gienek")
    m.join_match("gienek", "dominik")
    
    m.start_match("gienek", 1)
    
    print(m.get_state_for_match("gienek"))
    
    m.report_position_of("gienek", 50.06193797886043, 19.936964199935105)
    
    previous = m.get_state_for_match("gienek")
    
    
    m.report_position_of("gienek", 50.061013211887946, 19.932678728027547)

    current = m.get_state_for_match("gienek")
    
    print(previous)
    print(current)
    
    goals = m.get_goals("gienek")
    
    print(goals)
    
    
if __name__ == '__main__':
    main()
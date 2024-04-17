from geopy.distance import geodesic

class Goal:
    def __init__(self, lon, lat, dist_meters=150):
        self.lon = lon
        self.lat = lat
        self.min_dist = dist_meters
        
        self.scorer = None 
        self.id = None 
        
    def report_position(self, player, lon, lat):
        if self.was_scored():
            return
        
        dist = geodesic((self.lat, self.lon), (lat, lon)).meters
                        
        if dist < self.min_dist:
            self.scorer = player
        
    def was_scored(self):
        return self.scorer is not None
    
    def get_scorer(self):
        return self.scorer
    
    def get_id(self):
        return self.id 
    
    def set_id(self, id):
        self.id = id
    
class GoalTracker:
    def __init__(self, target_coords, win_threshold=5):
        self.target_coords = target_coords
        
        goals = []
        
        for coordinates in target_coords:
            lon = coordinates[0]
            lat = coordinates[1]
            
            goal = Goal(lon, lat)
            
            goals.append(goal)
            
        self.goals = goals
        self.win_threshold = win_threshold
        
    def report_position(self, player, lon, lat):
        for goal in self.goals:
            goal.report_position(player, lon, lat)
    
    def get_scores(self):
        scorers = [goal.get_scorer() for goal in self.goals if goal.was_scored()]
        
        scores = {}
        
        for scorer in scorers:
            if scorer in scores:
                scores[scorer] += 1
            else:
                scores[scorer] = 1

        return scores
    
    def get_target_coords(self):
        return self.target_coords
    
    def get_target_coords_with_scorers(self):
        goals = self.goals 
        
        def target_iteration():
            for goal in goals:
                yield {"id": goal.get_id(), "lon": goal.lon, "lat": goal.lat, "scorer": goal.get_scorer()}
    
        return list(target_iteration())
    
    def get_winner(self):
        scores = self.get_scores()
        
        for player, score in scores.items():
            if score >= self.win_threshold:
                return player
        
        return None
    
    def is_won(self):
        return self.get_winner() is not None

def get_goal_tracker_with_simple_goals():
    from geo import get_simple_location_generator
    
    locations = get_simple_location_generator().sample_locations(9)
    
    return GoalTracker(locations)
        
def main():
    
    goal = Goal(
        50.06266712680178,
        19.936899833446535)
    
    print(goal.was_scored())
    print(goal.get_scorer())
    
    goal.report_position("player1",
        0,
        0    
    )
    
    print(goal.was_scored())
    print(goal.get_scorer())
    
    goal.report_position("player1",
        50.061456095501114,
        19.936822259124053)
    
    print(goal.was_scored())
    print(goal.get_scorer())
    
    tracker = GoalTracker([
        (50.06266712680178, 19.936899833446535),
        (50.061456095501114, 19.936822259124053),
        (0, 0)
    ])
    
    tracker.report_position("dominik", 50.06266712680178, 19.936899833446535)
    tracker.report_position("gienek", 50.061456095501114, 19.936822259124053)
    tracker.report_position("gienek", 0, 0)
    
    print(tracker.get_scores())
    print(tracker.get_target_coords_with_scorers())
    
if __name__ == "__main__":
    main()

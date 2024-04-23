import unittest

from player_matcher import PlayerMatcher

class TestBasicMatching(unittest.TestCase):

    def test_player_not_in_match_can_participate(self):
        matcher = PlayerMatcher()
        
        self.assertFalse(matcher.is_player_in_match("gienek"))
        self.assertTrue(matcher.can_participate_in_match("gienek"))
    
    def test_cannot_join_nonexistent_match(self):
        matcher = PlayerMatcher()
        
        self.assertFalse(matcher.can_join_match("dominik", "dominik"))
    
    def test_cannot_leave_nonexistent_match(self):
        matcher = PlayerMatcher()
        
        self.assertFalse(matcher.can_leave_match("dominik"))
        
    def test_can_leave_existing_match(self):
        matcher = PlayerMatcher()
        
        matcher.create_match("michał154")
        
        self.assertTrue(matcher.can_leave_match("michał154"))
        
        matcher.leave_match("michał154")
        
        self.assertFalse(matcher.can_leave_match("michał154"))
        self.assertTrue(matcher.get_matches() == [])
        
    def test_cannot_participate_in_match_if_already_participating(self):
        matcher = PlayerMatcher()
        
        matcher.create_match("mietek")
        
        self.assertFalse(matcher.can_participate_in_match("mietek"))
        
    def test_another_people_can_join(self):
        matcher = PlayerMatcher()
        
        matcher.create_match("maciej")
        
        self.assertTrue(matcher.can_join_match("maciej", "mietek"))
        
        matcher.join_match("maciej", "mietek")
        
        self.assertFalse(matcher.can_join_match("maciej", "mietek"))
        self.assertFalse(matcher.can_join_match("maciej", "maciej"))
        self.assertFalse(matcher.can_join_match("maciej", "dominik"))

    def test_cannot_start_one_person_match(self):
        matcher = PlayerMatcher()
        
        matcher.create_match("mietek")
        
        self.assertFalse(matcher.can_game_be_started("mietek"))
        self.assertRaises(ValueError, matcher.start_match, "mietek", 1)
        
    def test_game_can_be_won(self):
        matcher = PlayerMatcher()
        
        matcher.create_match("mietek")
        matcher.join_match("mietek", "maciej")
        
        matcher.start_match("mietek", 1)
        
        self.assertTrue(matcher.get_winner("mietek") == None)
        
        import csv 
        
        locations = csv.reader(open("locations.csv"))
        
        for location in locations:
            matcher.report_position_of("mietek", location[0], location[1])
            matcher.report_position_of("maciej", location[0], location[1])
        
        self.assertTrue(matcher.get_winner("mietek") == "mietek")
        self.assertTrue(matcher.get_winner("maciej") == "mietek")
    
if __name__ == '__main__':
    unittest.main()
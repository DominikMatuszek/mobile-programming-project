import random 
import csv 

class FixedListLocationGenerator:
    def __init__(self, location_csv):
        csv_file = csv.reader(open(location_csv, "r"))
        
        locations = []
        
        for row in csv_file:
            if len(row) == 2:
                locations.append(row)
            
        self.locations = locations
            
    def sample_locations(self, n):
        return random.sample(self.locations, n)
    
    def get_all_locations(self):
        return self.locations
    
def main():
    location_generator = FixedListLocationGenerator("locations.csv")
    
if __name__ == "__main__":
    main()
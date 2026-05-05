package com.contactpro.contactpro.util;

import java.util.HashSet;
import java.util.Set;
import java.util.Arrays;

public class GenderPredictor {

    private static final Set<String> MALE_NAMES = new HashSet<>(Arrays.asList(
        // Indian male names
        "adithya", "aditya", "abhinav", "abhishek", "ajay", "ajit", "akash", "akhil", "akshay",
        "amar", "amit", "amith", "anand", "anil", "anirudh", "anjaneya", "ankit", "ankur",
        "anoop", "ansh", "anuj", "arjun", "arun", "aryan", "ashish", "ashok", "ashwin",
        "bala", "balaji", "bharath", "bharat", "bhaskar", "bhavesh", "chandan", "chetan",
        "chirag", "darshan", "deepak", "dev", "devendra", "dhruv", "dilip", "dinesh",
        "ganesh", "gaurav", "girish", "gopal", "govind", "gururaj", "hari", "harish",
        "hemant", "hitesh", "jagdish", "jai", "jatin", "jayesh", "jeevan", "jitendra",
        "karan", "kartik", "karthik", "keshav", "kishan", "krishna", "kumar", "kunal",
        "lakshman", "lokesh", "madhav", "mahesh", "manoj", "manish", "mithun", "mohan",
        "mohit", "mukesh", "naga", "nagesh", "naresh", "narendra", "naveen", "neeraj",
        "nikhil", "nilesh", "nitin", "pankaj", "paresh", "parth", "pavan", "pramod",
        "pranav", "prasad", "prashant", "pratap", "pratik", "praveen", "prem", "prithvi",
        "raghu", "rahul", "raj", "rajat", "rajesh", "raju", "rakesh", "ram", "ramesh",
        "ranjith", "ravi", "rishabh", "ritesh", "rohit", "sachin", "sagar", "sahil",
        "sai", "sandeep", "sanjay", "sanjiv", "santosh", "sarath", "satish", "shailesh",
        "sharath", "shiva", "shivam", "shubham", "siddharth", "soma", "soham", "srikanth",
        "srihari", "srinivas", "subhash", "sudhir", "sumit", "sundar", "sunil", "suresh",
        "surya", "tarun", "tushar", "uday", "ujjwal", "vaibhav", "varun", "venkat",
        "venkatesh", "vijay", "vikas", "vikram", "vinay", "vinod", "vipul", "vishnu",
        "vivek", "yogesh", "yuva", "yash",
        // Global male names
        "john", "david", "michael", "james", "robert", "william", "daniel", "thomas",
        "mohammed", "ahmed", "ali", "omar", "carlos", "jose", "pedro", "juan",
        "alexander", "andrew", "brian", "chris", "eric", "kevin", "mark", "paul",
        "ryan", "scott", "steven", "jason", "adam", "peter", "george", "henry"
    ));

    private static final Set<String> FEMALE_NAMES = new HashSet<>(Arrays.asList(
        // Indian female names
        "aarti", "aditi", "akshara", "amita", "amrita", "ananya", "anita", "anju",
        "ankita", "anu", "anusha", "aparna", "archana", "asha", "bhavana", "bharati",
        "chaitra", "chandana", "chitra", "deepa", "deepika", "devi", "divya", "durga",
        "geetha", "geeta", "hema", "indira", "jaya", "jayashree", "jyoti", "jyothi",
        "kala", "kamala", "kavitha", "kavya", "keerthi", "komal", "latha", "laxmi",
        "lakshmi", "madhavi", "mamta", "manasa", "meena", "meenakshi", "megha", "mohini",
        "nalini", "namita", "nandini", "neha", "nisha", "nithya", "padma", "pallavi",
        "pavithra", "pooja", "puja", "priya", "priyanka", "radha", "rajeshwari", "ranjani",
        "rashmi", "rekha", "renuka", "revathi", "ritu", "rohini", "rupa", "ruchika",
        "sarita", "savita", "seema", "shanti", "sharada", "shreya", "shilpa", "shobha",
        "sita", "smita", "sneha", "sonali", "sonia", "sudha", "sujata", "sumathi",
        "sunita", "sushma", "swathi", "tara", "uma", "usha", "vandana", "vanitha",
        "varsha", "veena", "vidya", "vijaya", "vimala",
        // Global female names
        "mary", "sarah", "jessica", "jennifer", "emma", "olivia", "sophia", "isabella",
        "fatima", "aisha", "maria", "ana", "anna", "elena", "lisa", "laura",
        "rachel", "rebecca", "amanda", "emily", "hannah", "grace", "elizabeth"
    ));

    public static String predict(String name) {
        if (name == null || name.trim().isEmpty()) return "Prefer not to say";
        
        String firstName = name.trim().split("\\s+")[0].toLowerCase();
        
        if (MALE_NAMES.contains(firstName)) return "Male";
        if (FEMALE_NAMES.contains(firstName)) return "Female";
        
        return "Prefer not to say";
    }
}

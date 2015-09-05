#!/usr/sbin/ruby
require 'byebug'
data_dir = File.expand_path('../../translations/cards/[ combined ]', __FILE__)
card_csv = "#{data_dir}/en.csv"
data_csv = "#{data_dir}/data.csv"
supplies_csv = File.expand_path('../../translations/supplies/data.csv', __FILE__)
supply_names_csv = File.expand_path('../../translations/supplies/translation-en.csv', __FILE__)

################################################################################
def get_schema(file)
  File.open(file, 'r')
  first_line = File.open(file, &:readline)
  first_line.rstrip.split(';').map.with_index.to_h
end

def get_csv_data(input, card_data)
  card_names = input.split(/\s*,\s*/)

  cards = card_names.map do |name|
    card = card_data[name]
    if card.nil?
      puts "ERROR: Unrecognized card name: #{name}"
      return nil
    end
    card
  end

  card_ids = cards.map{|c| c[:id]}
  num_cards = cards.length
  high_cost = cards.select{|c| c[:set_name] == 'Prosperity'}.length >= (num_cards / 2)
  shelters = cards.select{|c| c[:set_name] == 'Dark Ages'}.length >= (num_cards / 2)
  bane = card_names.include?("Young Witch")
  puts "cards: #{card_ids.join(',')}"
  puts "high cost: #{high_cost}"
  puts "shelters: #{shelters}"
  puts "bane: #{bane}"
end

def get_input(prompt)
  $stdout.print prompt # Prompt
  input = gets.rstrip # Read user input
  exit 0 if %w(exit quit).include?(input) # Exit
  input
end

################################################################################
# Pull in the data for each card, and put it into an array of objects
card_data = {}
card_id_to_name = {}
schema = {}
# Get the basic values
schema = get_schema(card_csv)
File.open(card_csv, 'r').each_line do |line|
  line = line.rstrip

  parts = line.split(';')
  id = parts[schema['_id']]
  name = parts[schema['name']]

  card_id_to_name[id] = name
  card_data[name] = {
    :id => parts[schema['_id']],
    :language => parts[schema['language']],
    :set_name => parts[schema['set_name']],
    :name => parts[schema['name']],
    :type => parts[schema['type']],
    :requires => parts[schema['requires']],
    :description => parts[schema['description']]
  }
end
################################################################################
# REPL
while true # Continue until user enters exit
  input = get_input("Cards in Set: ")
  csv_line = get_csv_data(input, card_data)
  next if csv_line.nil?
end
exit 0

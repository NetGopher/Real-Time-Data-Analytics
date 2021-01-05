export class KafkaAction {
  static readonly type = '[Kafka] Add item';
  constructor(public payload: string) { }
}
export class AddMessageAction {
  static readonly type = '[Kafka] Add message';
  constructor(public message: string) { }
}

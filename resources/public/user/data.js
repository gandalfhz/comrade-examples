$(document).ready(function () {

  var ViewModel = function () {
    this.keyValues = ko.observableArray([]);
    this.key = ko.observable('key1');
    this.value = ko.observable('value1');
    this.response = ko.observable('');

    this.add = () =>
      $.ajax({
        type: 'POST',
        url: '/api/user/data/' + encodeURIComponent(this.key()),
        data: JSON.stringify({ value: this.value() }),
        dataType: 'json',
        contentType: 'application/json; charset=utf-8',
        success: (response) => {
          this.response('Added.');
          this.loadKeys();
        },

        error: () => this.response('Failed.'),
      });

    this.loadValue = (keyValue) =>
      $.ajax({
        type: 'GET',
        url: '/api/user/data/' + encodeURIComponent(keyValue.key),
        success: (response) => keyValue.value(response),
        error: () => this.response('Failed.'),
      });

    // CLEAN UP ALL STYLE ERRORS. DO THE ADMIN PAGE.
    // DO A SUPER SIMPLE EXAMPLE FILE TOO,
    // WITH JUST A HELLO AND A BUTTON THAT WILL DO A LOGIN,
    // CALL A PROTECTED PAGE, AND THEN HAVE A JSON GET
    // ON THE PROTECTED PAGE.

    this.deleteKeyValue = (keyValue) =>
      $.ajax({
        type: 'DELETE',
        url: '/api/user/data/' + encodeURIComponent(keyValue.key),
        success: (response) => this.keyValues.remove(keyValue),
        error: () => this.response('Failed.'),
      });

    this.loadKeys = () =>
      $.ajax({
        type: 'GET',
        url: '/api/user/data',
        success: (response) => this.keyValues(response.map((k) => {
          return {
            key: k,
            uri: '/user/data/' + k,
            value: ko.observable(''),
          };
        })),
        error: () => this.response('Failed.'),
      });

    this.logout = () =>
      $.ajax({
        type: 'GET',
        url: '/api/logout',
        success: (response) => location.href = '/',
        error: () => this.response('Failed.'),
      });
  };

  var viewModel = new ViewModel();
  ko.applyBindings(viewModel);
  viewModel.loadKeys();
});
